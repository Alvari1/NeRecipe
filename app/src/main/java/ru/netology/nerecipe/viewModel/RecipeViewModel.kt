package ru.netology.nerecipe.viewModel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.view.View
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.netology.nerecipe.R
import ru.netology.nerecipe.adapter.IngredientInteractionListener
import ru.netology.nerecipe.adapter.RecipeInteractionListener
import ru.netology.nerecipe.adapter.StepInteractionListener
import ru.netology.nerecipe.data.CuisineRepository
import ru.netology.nerecipe.data.IngredientRepository
import ru.netology.nerecipe.data.IngredientRepository.Companion.EMPTY_INGREDIENT
import ru.netology.nerecipe.data.RecipeRepository
import ru.netology.nerecipe.data.RecipeRepository.Companion.EMPTY_RECIPE
import ru.netology.nerecipe.data.StepRepository
import ru.netology.nerecipe.data.StepRepository.Companion.EMPTY_STEP
import ru.netology.nerecipe.data.impl.CuisineRepositoryDao
import ru.netology.nerecipe.data.impl.IngredientRepositoryDao
import ru.netology.nerecipe.data.impl.RecipeRepositoryDao
import ru.netology.nerecipe.data.impl.StepRepositoryDao
import ru.netology.nerecipe.db.AppDb
import ru.netology.nerecipe.dto.*
import ru.netology.nerecipe.enums.ActionState
import ru.netology.nerecipe.util.*
import java.io.File
import java.util.*

class RecipeViewModel(application: Application) : AndroidViewModel(application),
    RecipeInteractionListener, StepInteractionListener, IngredientInteractionListener {
    // recipe section
    private val repository: RecipeRepository = RecipeRepositoryDao(
        AppDb.getInstance(context = application).recipeDao()
    )

    val data = repository.getAll(getApplication())
    private val currentRecipe by repository::currentRecipe
    val editRecipe = MutableLiveData<Recipe>()
    private val filteredRecipeIds = MutableLiveData<Set<UUID>?>(setOf(ALL_UUID))
    val feedRecipes = MutableLiveData<List<Recipe>>()

    // step section
    private val stepRepository: StepRepository = StepRepositoryDao(
        AppDb.getInstance(context = application).stepDao()
    )
    val stepData = stepRepository.getAll(getApplication())
    private val currentSteps by stepRepository::currentSteps
    private val activeStep by stepRepository::activeStep
    private val activeStepPosition = MutableLiveData<Int?>()
    val navigateCurrentStepsChanged = SingleLiveEvent<Boolean>()

    // ingredient section
    private val ingredientRepository: IngredientRepository = IngredientRepositoryDao(
        AppDb.getInstance(context = application).ingredientDao()
    )
    val ingredientData = ingredientRepository.getAll()
    private val currentIngredients by ingredientRepository::currentIngredients
    val navigateCurrentIngredientsChanged = SingleLiveEvent<Boolean>()

    // cuisine section
    private val cuisineRepository: CuisineRepository = CuisineRepositoryDao(
        AppDb.getInstance(context = application).cuisineDao()
    )
    val cuisineData = cuisineRepository.getAll()
    private val currentSelectedCuisines by cuisineRepository::currentSelectedCuisines
    val navigateCuisineFilterSetBadges = SingleLiveEvent<Boolean>()
    val navigateCuisineFilterUpdate = MutableLiveData<Boolean>()
    val navigateAllCuisinesCheckbox = MutableLiveData<Boolean>()

    //trackers
    var imageChangedTracker = HashMap<UUID, Boolean>()
    var imageForDeleteTracker = HashMap<String, Boolean>()

    // navigation
    val navigateFromFeed = SingleLiveEvent<Boolean>()
    val navigateFeedView = SingleLiveEvent<Boolean>()

    val navigateFromRecipe = SingleLiveEvent<Boolean>()
    val navigateRecipeSave = SingleLiveEvent<Boolean>()
    val navigateFromFavorites = SingleLiveEvent<Boolean>()
    val navigateRecipeCancel = SingleLiveEvent<Boolean>()
    val navigateRecipeDelete = MutableLiveData<Recipe>()
    val navigateRecipeShare = SingleLiveEvent<String?>()

    //call after image pickup
    val navigateRecipeGetImageFromGallery = SingleLiveEvent<Boolean>()
    val navigateRecipeGetImageFromCamera = SingleLiveEvent<Boolean>()
    val navigateStepGetImageFromGallery = SingleLiveEvent<Boolean>()
    val navigateStepGetImageFromCamera = SingleLiveEvent<Boolean>()

    private var appBarState = AppBarState()
    val navigateRecipeAppBarState = SingleLiveEvent<AppBarState?>()

    private var currentActionState: ActionState = ActionState.FEED
    private var previousActionState: ActionState = ActionState.FEED
    val navigateActionStateChanged = SingleLiveEvent<Boolean?>()

    val navigateLargeImageView = SingleLiveEvent<Boolean?>()
    private var largeImage = LargeImageStore(appBarState = appBarState)
    private var zoomMode: Boolean = false

    val navigateSnackbar = SingleLiveEvent<SnackbarStore>()

    private var tabSelected: Int? = null

    fun onSaveButtonClicked(recipe: Recipe?) {
        if (recipe == null) return

        setActionState(previousActionState)

        currentRecipe.value = recipe.copy(
            id = if (recipe.id == EMPTY_UUID)
                getNextId()
            else recipe.id,
            title = recipe.title.trim(),
            author = recipe.author.trim(),
            description = recipe.description?.trim(),
            published = if (recipe.id == EMPTY_UUID)
                System.currentTimeMillis()
            else recipe.published,
            image = if (imageChangedTracker[recipe.id] == null) recipe.image
            else saveBitmapToFileOrDeleteOldImagesIfBitmapIsNull(
                recipe.image,
                recipe.imageBitmap,
                viewModelScope
            )
        )

        repository.save(currentRecipe.value!!)
        editRecipe.value = currentRecipe.value

        shrinkCurrentSteps(currentRecipe.value!!.id)
        currentSteps.value = currentSteps.value!!.map {
            it.copy(
                image = if (imageChangedTracker[it.id] == null) it.image else
                    saveBitmapToFileOrDeleteOldImagesIfBitmapIsNull(
                        it.image,
                        it.imageBitmap,
                        viewModelScope
                    )
            )
        }
        stepRepository.save(currentSteps.value!!)

        shrinkCurrentIngredients(currentRecipe.value!!.id)
        if (isEmptyIngredientsList(currentIngredients.value!!))
            ingredientRepository.deleteByRecipeId(currentRecipe.value!!.id)
        else
            ingredientRepository.save(currentIngredients.value!!)

        deleteImagesFromImageForDeleteTracker()

        navigateCurrentStepsChanged.call()
        navigateCurrentIngredientsChanged.call()
        navigateRecipeSave.call()
    }

    override fun onRecipeLikeClicked(recipe: Recipe?) {
        if (recipe == null) return

        repository.like(recipe.id)

        if (currentActionState == ActionState.PREVIEW) updateLikeForCurrentRecipe()
    }

    private fun updateLikeForCurrentRecipe() {
        val tempRecipe = currentRecipe.value
        if (tempRecipe != null) {
            currentRecipe.value = tempRecipe.copy(
                likedByMe = !tempRecipe.likedByMe,
                likes = if (tempRecipe.likedByMe) tempRecipe.likes - 1 else tempRecipe.likes + 1
            )
            editRecipe.value = currentRecipe.value
        }
    }

    override fun onRecipeShareClicked(recipe: Recipe?) {
        if (recipe == null) return

        repository.share(recipe.id)
        if (currentActionState == ActionState.PREVIEW) updateShareForCurrentRecipe()

        navigateRecipeShare.value = constructRecipeForShare(recipe)
    }

    private fun updateShareForCurrentRecipe() {
        val tempRecipe = currentRecipe.value
        if (tempRecipe != null) {
            currentRecipe.value = tempRecipe.copy(shares = tempRecipe.shares + 1)
            editRecipe.value = currentRecipe.value
        }
    }

    override fun onRecipeDeleteClicked(recipe: Recipe?) {
        if (recipe == null) return

        deleteImagesFiles(recipe.image, viewModelScope)
        repository.delete(getApplication(), recipe)
        getSteps(recipe, false)?.let { list ->
            list.mapNotNull { step -> step.image }.forEach { deleteImagesFiles(it, viewModelScope) }
            stepRepository.deleteByRecipeId(recipe.id)
        }

        ingredientRepository.deleteByRecipeId(recipe.id)

        deleteImagesFromImageForDeleteTracker()
        clearCurrents()
    }

    fun recipeDeleteConfirmDialog(recipe: Recipe?) {
        if (recipe == null) return

        navigateRecipeDelete.value = recipe!!
    }

    override fun onRecipeEditClicked(recipe: Recipe?) {
        if (recipe == null) return

        setActionState(ActionState.EDIT)

        currentRecipe.value = getRecipe(recipe)
        editRecipe.value = currentRecipe.value

        currentSteps.value = getSteps(recipe)

        currentIngredients.value = getIngredients(recipe)

        setRecipeAppBarState(isSave = true, isCancel = true, isDelete = true)

        when (previousActionState) {
            ActionState.FEED -> navigateFromFeed.call()
            ActionState.FAVORITES -> navigateFromFavorites.call()
            else -> navigateFromRecipe.call()
        }
    }

    override fun onRecipeItemClicked(recipe: Recipe?) {
        if (recipe == null) return

        setActionState(ActionState.PREVIEW)

        repository.view(recipe.id)

        currentRecipe.value = getRecipe(recipe)
        editRecipe.value = currentRecipe.value

        currentSteps.value = getSteps(recipe)

        currentIngredients.value = getIngredients(recipe)

        setRecipeAppBarState(isEdit = true, isDelete = true)

        when (previousActionState) {
            ActionState.FEED -> navigateFromFeed.call()
            ActionState.FAVORITES -> navigateFromFavorites.call()
            else -> navigateFromRecipe.call()
        }
    }

    override fun onCancelEditClicked(recipe: Recipe?) {
        if (recipe == null) return

        setActionState(previousActionState)

        currentRecipe.value = getRecipe(recipe)
        editRecipe.value = currentRecipe.value

        currentSteps.value = getSteps(recipe)

        currentIngredients.value = getIngredients(recipe)

        setRecipeAppBarState(isEdit = true, isDelete = true)

        imageForDeleteTracker.clear()
        navigateRecipeCancel.call()
    }

    fun onAddClicked() {
        if (currentActionState == ActionState.EDIT) {
            when (tabSelected) {
                1 -> { //steps
                    currentRecipe.value?.let { addEmptyStep(it.id) }
                }
                2 -> { //ingredients
                    currentRecipe.value?.let { addEmptyIngredient(it.id) }
                }
            }
        }
    }

    fun validateCurrentsSaveConditions(): Boolean {
        val result = if (!currentRecipe.value?.title?.trim().isNullOrEmpty() &&
            !currentRecipe.value?.author?.trim().isNullOrEmpty()
        )
            currentSteps.value?.none { it.description.trim().isNotBlank() } == false
        else false

        setRecipeAppBarStateByObject(appBarState.copy(isSave = result))
        return result
    }

    fun updateCurrentRecipeTitle(content: String?) {
        if (currentRecipe.value != null) {
            currentRecipe.value = currentRecipe.value!!.copy(title = content ?: "")
            validateCurrentsSaveConditions()
        }
    }

    fun updateCurrentRecipeAuthor(content: String?) {
        if (currentRecipe.value != null) {
            currentRecipe.value = currentRecipe.value!!.copy(author = content ?: "")
            validateCurrentsSaveConditions()
        }
    }

    fun updateCurrentRecipeDescription(content: String?) {
        if (currentRecipe.value != null)
            currentRecipe.value = currentRecipe.value!!.copy(description = content ?: "")
    }

    fun updateCurrentRecipeCuisine(resId: Int) {
        if (currentRecipe.value != null)
            currentRecipe.value = currentRecipe.value!!.copy(cuisine = resId)
    }

    fun getCurrentRecipe() = currentRecipe.value

    private fun getRecipe(recipe: Recipe?): Recipe =
        if (recipe == null) {
            EMPTY_RECIPE
        } else {
            if (recipe.image != null) {
                val image = getImageFromStorage(recipe.image)
                if (image != null) recipe.copy(imageBitmap = image) else recipe
            } else
                recipe
        }

    private fun getStepsByRecipeId(recipeId: UUID) =
        stepData.value?.filter { it.recipeId == recipeId }

    private fun shrinkCurrentSteps(recipeId: UUID) {
        val tempValue = currentSteps.value?.filter { it.description.trim().isNotBlank() }

        currentSteps.value?.filter { it.description.trim().isBlank() && it.image != null }
            ?.forEach { step -> imageForDeleteTracker[step.image!!] = true }

        if (tempValue.isNullOrEmpty()) currentSteps.value = emptyStepList(recipeId)
        else
            currentSteps.value = tempValue.mapIndexed { index, step ->
                step.copy(
                    recipeId = recipeId,
                    position = index
                )
            }
    }

    private fun emptyStepList(recipeId: UUID) = listOf(
        EMPTY_STEP.copy(
            id = getNextId(),
            recipeId = recipeId,
            position = 0,
            description = "",
        )
    )

    override fun addEmptyStep(recipeId: UUID) {
        val tempValue =
            EMPTY_STEP.copy(
                id = getNextId(),
                description = "",
                recipeId = recipeId,
                position = currentSteps.value?.size ?: 0,
            )

        val tempList = currentSteps.value?.toMutableList()
        if (!tempList.isNullOrEmpty()) Collections.addAll(tempList, tempValue)

        currentSteps.value = tempList?.toList()

        navigateCurrentStepsChanged.call()
    }

    override fun removeStep(step: Step) {
        if (step.image != null) imageForDeleteTracker[step.image] = true

        val tempSteps = currentSteps.value?.filter { it.id != step.id }
        if (!tempSteps.isNullOrEmpty()) {
            currentSteps.value =
                tempSteps.mapIndexed { index, currStep -> currStep.copy(position = index) }
            navigateCurrentStepsChanged.call()
        }
    }

    fun getCurrentSteps() = currentSteps.value

    fun swapCurrentSteps(startPosition: Int, endPosition: Int) {
        val tempList = currentSteps.value?.toMutableList()

        if (!tempList.isNullOrEmpty())
            Collections.swap(tempList, startPosition, endPosition)

        currentSteps.value = tempList?.toList()
    }

    private fun getSteps(recipe: Recipe, returnEmptyStep: Boolean = true): List<Step>? {
        val tempSteps = stepData.value?.filter { it.recipeId == recipe.id }

        return if (tempSteps.isNullOrEmpty() && returnEmptyStep) {
            emptyStepList(recipe.id)
        } else {
            val tempStepsWithImageBitmap = tempSteps?.toMutableList()

            if (!tempSteps.isNullOrEmpty()) {
                tempSteps.filter { it.image != null }.forEachIndexed { index, step ->
                    val image = getImageFromStorage(step.image!!)
                    if (image != null) tempStepsWithImageBitmap!![index] =
                        step.copy(imageBitmap = image)
                }
            }
            tempStepsWithImageBitmap?.toList()
        }
    }


    fun updateStepDescription(id: UUID, content: String?) {
        //don't update editSteps
        val steps = currentSteps.value?.toMutableList()
        if (!steps.isNullOrEmpty()) {
            steps.forEachIndexed { index, stepItem ->
                stepItem.takeIf { it.id == id }
                    ?.let {
                        steps[index] = it.copy(description = content ?: "")
                    }
            }
            currentSteps.value = steps.toList()
            validateCurrentsSaveConditions()
        }
    }

    fun setActiveStep(step: Step?, position: Int) {
        activeStep.value = step
        activeStepPosition.value = position
    }

    fun getActiveStep() = activeStep.value

    fun getActiveStepPosition() = activeStepPosition.value

    private fun emptyIngredientList(recipeId: UUID) = listOf(
        EMPTY_INGREDIENT.copy(
            id = getNextId(),
            recipeId = recipeId,
            description = "",
            position = 0,
        )
    )

    private fun getIngredientsByRecipeId(recipeId: UUID) =
        ingredientData.value?.filter { it.recipeId == recipeId }

    private fun isEmptyIngredientsList(ingredients: List<Ingredient>) =
        ingredients.size == 1 && ingredients.first().description.isEmpty()

    private fun shrinkCurrentIngredients(recipeId: UUID) {
        val tempValue = currentIngredients.value?.filter { it.description.trim().isNotBlank() }

        if (tempValue.isNullOrEmpty())
            currentIngredients.value = emptyIngredientList(recipeId)
        else
            currentIngredients.value = tempValue.mapIndexed { index, ingredient ->
                ingredient.copy(recipeId = recipeId, position = index)
            }
    }

    override fun addEmptyIngredient(recipeId: UUID) {
        val tempValue =
            EMPTY_INGREDIENT.copy(
                id = getNextId(),
                recipeId = recipeId,
                description = "",
                position = currentIngredients.value?.size ?: 0,
            )

        val tempList = currentIngredients.value?.toMutableList()
        if (!tempList.isNullOrEmpty())
            Collections.addAll(tempList, tempValue)
        currentIngredients.value = tempList?.toList()

        navigateCurrentIngredientsChanged.call()
    }

    override fun removeIngredient(id: UUID) {
        val tempIngredients = currentIngredients.value?.filter { it.id != id }

        if (!tempIngredients.isNullOrEmpty()) {
            currentIngredients.value =
                tempIngredients.mapIndexed { index, currIngredient ->
                    currIngredient.copy(
                        position = index
                    )
                }
        }

        navigateCurrentIngredientsChanged.call()
    }

    fun getCurrentIngredients() = currentIngredients.value

    private fun getIngredients(
        recipe: Recipe,
        returnEmptyStep: Boolean = true
    ): List<Ingredient>? {
        val tempIngredients = ingredientData.value?.filter { it.recipeId == recipe.id }

        return if (tempIngredients.isNullOrEmpty() && returnEmptyStep)
            emptyIngredientList(recipe.id)
        else tempIngredients
    }

    fun updateIngredientDescription(ingredient: Ingredient?) {
        //don't update editIngredients
        if (ingredient != null) {
            val ingredients = currentIngredients.value?.toMutableList()
            if (!ingredients.isNullOrEmpty()) {
                ingredients.forEachIndexed { index, ingredientItem ->
                    ingredientItem.takeIf {
                        it.recipeId == ingredient.recipeId && it.id == ingredient.id
                    }
                        ?.let {
                            ingredients[index] = it.copy(description = ingredient.description)
                        }
                }
                currentIngredients.value = ingredients.toList()
            }
        }
    }

    fun swapCurrentIngredients(startPosition: Int, endPosition: Int) {
        val tempList = currentIngredients.value?.toMutableList()

        if (tempList != null)
            Collections.swap(tempList, startPosition, endPosition)

        currentIngredients.value = tempList?.toList()
    }

    fun clearCurrents() {
        currentRecipe.value = null
        editRecipe.value = currentRecipe.value
        imageChangedTracker.clear()

        currentSteps.value = null
        activeStep.value = null
        activeStepPosition.value = null

        currentIngredients.value = null

        setRecipeAppBarState()
    }

    fun createEmptyCurrentRecipeWithStepsAndIngredients() {
        currentRecipe.value = getRecipe(null)
        editRecipe.value = currentRecipe.value

        currentSteps.value = getSteps(currentRecipe.value!!)
        activeStep.value = null
        activeStepPosition.value = null

        currentIngredients.value = getIngredients(currentRecipe.value!!)

        imageChangedTracker.clear()
        imageForDeleteTracker.clear()
    }

    fun setRecipeAppBarState(
        isSearch: Boolean = false,
        isAdd: Boolean = false,
        isSave: Boolean = false,
        isEdit: Boolean = false,
        isCancel: Boolean = false,
        isDelete: Boolean = false
    ) {
        appBarState = AppBarState(
            isSearch = isSearch,
            isAdd = isAdd,
            isSave = isSave,
            isEdit = isEdit,
            isCancel = isCancel,
            isDelete = isDelete
        )

        navigateRecipeAppBarState.value = appBarState
    }

    fun setRecipeAppBarStateByObject(state: AppBarState) {
        appBarState = state
        navigateRecipeAppBarState.value = state
    }

    fun getRecipeAppBarState() = appBarState

    fun setActionState(state: ActionState) {
        previousActionState = currentActionState
        currentActionState = state

        navigateActionStateChanged.call()
    }

    fun getActionState(): ActionState = currentActionState

    fun getfilteredRecipeIds() = filteredRecipeIds.value

    private fun sanitizeSearchQuery(query: String?): String {
        if (query == null) {
            return "";
        }
        val cleanQuery =
            query.replace("[^\\w\\s]", "")
        return "%$cleanQuery%"
    }

    fun filterSQLSearch(content: String?) {
        if (!content.isNullOrEmpty()) {
            val sanitizedQuery = sanitizeSearchQuery(content)
            val allIdSet = repository.searchDatabase(sanitizedQuery)
            filteredRecipeIds.value = allIdSet.toSet()
        } else
            filteredRecipeIds.value = setOf(ALL_UUID)

        updateFeedRecipes()
    }


    fun initializeSelectedCuisines(updateBadges: Boolean = false) {
        currentSelectedCuisines.value =
            cuisineData.value?.filter { it.isSelected }?.map { it.id }?.toSet()?.toList()
        if (currentSelectedCuisines.value?.size == cuisineData.value?.size)
            addCuisineToCurrentSelectedCuisines(-1, updateBadges = updateBadges)
    }

    fun getCuisinesText(): MutableMap<Int, String> {
        val cuisinesText: MutableMap<Int, String> = emptyMap<Int, String>().toMutableMap()
        cuisineData.value?.forEach { cuisinesText[it.id] = it.resName }

        return if (cuisinesText.isEmpty()) emptyMap<Int, String>().toMutableMap() else
            cuisinesText
    }

    fun setAllCuisinesChecked() {
        val tempCuisines = cuisineData.value?.map { cuisine ->
            cuisine.copy(isSelected = true)
        }
        if (!tempCuisines.isNullOrEmpty()) {
            cuisineRepository.saveAll(tempCuisines)
            currentSelectedCuisines.value = tempCuisines.map { it.id }.toSet().toList()
            currentSelectedCuisines.value?.get(0)
                ?.let { addCuisineToCurrentSelectedCuisines(-1, updateBadges = true) }
        }
    }

    fun getCuisineNameById(resId: Int): String =
        cuisineData.value?.filter { it.id == resId }?.map { it.resName }?.get(0) ?: ""

    fun getCheckedCuisinesCount() = cuisineData.value?.filter { it.isSelected }?.size ?: 0

    fun getCurrentSelectedCuisines() = currentSelectedCuisines.value

    fun addCuisineToCurrentSelectedCuisines(
        item: Int,
        updateData: Boolean = false,
        updateBadges: Boolean = false
    ) {
        var tempValue =
            currentSelectedCuisines.value?.toSet()?.plus(item)

        if (tempValue?.filter { it != -1 }?.size == cuisineData.value?.size) {
            tempValue?.plus(-1)
            navigateAllCuisinesCheckbox.value = true
        } else {
            tempValue = tempValue?.toSet()?.filter { it != -1 }?.toSet()
            navigateAllCuisinesCheckbox.value = false
        }

        currentSelectedCuisines.value = tempValue?.toSet()?.toList()

        if (updateBadges)
            navigateCuisineFilterSetBadges.call()

        if (updateData) {
            val cuisine = cuisineData.value?.filter { it.id == item }?.get(0)
            if (cuisine != null) cuisineRepository.save(cuisine.copy(isSelected = true))
        }
    }

    fun removeCuisineFromCurrentSelectedCuisines(item: Int, updateData: Boolean = false) {
        val tempValue =
            currentSelectedCuisines.value?.toSet()?.filter { it != item && it != -1 }?.toSet()
                ?.toList()
        currentSelectedCuisines.value = tempValue?.toList()

        navigateAllCuisinesCheckbox.value = false
        navigateCuisineFilterSetBadges.call()

        if (updateData) {
            val cuisine = cuisineData.value?.filter { it.id == item }?.get(0)
            if (cuisine != null) cuisineRepository.save(cuisine.copy(isSelected = false))
        }
    }

    fun setCuisineFilterValue(value: Boolean) {
        navigateCuisineFilterUpdate.value = value
    }

    fun setTabSelected(tab: Int?) {
        tabSelected = tab
    }

    fun getTabSelected() = tabSelected

    fun setCurrentRecipeImage(image: Bitmap?) {
        currentRecipe.value =
            currentRecipe.value?.copy(imageBitmap = image)

        editRecipe.value = currentRecipe.value
        imageChangedTracker[currentRecipe.value!!.id] = true
    }

    fun deleteCurrentRecipeImage() {
        setCurrentRecipeImage(null)
    }

    fun rotateCurrentRecipeImage() {
        if (currentRecipe.value?.imageBitmap != null) {
            val image = currentRecipe.value?.imageBitmap?.rotate(90f)

            currentRecipe.value = currentRecipe.value?.copy(imageBitmap = image)
            imageChangedTracker[currentRecipe.value!!.id] = true

            editRecipe.value = currentRecipe.value
        }
    }

    fun setActiveStepImage(image: Bitmap?) {
        if (activeStep.value != null) {
            val steps = currentSteps.value?.toMutableList()
            if (!steps.isNullOrEmpty()) {
                steps.forEachIndexed { index, stepItem ->
                    stepItem.takeIf { it.id == activeStep.value!!.id }
                        ?.let { steps[index] = it.copy(imageBitmap = image) }
                }
                currentSteps.value = steps.toList()

                imageChangedTracker[activeStep.value!!.id] = true
                navigateCurrentStepsChanged.call()
            }
        }
    }

    fun deleteActiveStepImage() {
        setActiveStepImage(null)
    }

    fun rotateCurrentStepImage() {
        if (activeStep.value != null) {
            if (activeStep.value!!.imageBitmap != null) {
                if (!currentSteps.value.isNullOrEmpty()) {
                    val steps = currentSteps.value?.toMutableList()
                    var image =
                        activeStep.value!!.imageBitmap
                    image = image!!.rotate(90f)

                    if (!steps.isNullOrEmpty()) {
                        steps.forEachIndexed { index, stepItem ->
                            stepItem.takeIf { it.id == activeStep.value!!.id }
                                ?.let { steps[index] = it.copy(imageBitmap = image) }
                        }
                    }

                    currentSteps.value = steps?.toList()

                    imageChangedTracker[activeStep.value!!.id] = true
                    navigateCurrentStepsChanged.call()
                }
            }
        }
    }

    fun onGetRecipeImageFromCameraClicked() {
        navigateRecipeGetImageFromCamera.call()
    }

    fun onGetRecipeImageFromGalleryClicked() {
        navigateRecipeGetImageFromGallery.call()
    }

    fun onGetStepImageFromCameraClicked() {
        navigateStepGetImageFromCamera.call()
    }

    fun onGetStepImageFromGalleryClicked() {
        navigateStepGetImageFromGallery.call()
    }

    fun updateFeedRecipes() {
        val items = getFilteredData(data.value)

        if (items == null) feedRecipes.value = listOf()
        else feedRecipes.value = items.toList()
    }

    private fun getFilteredData(recipes: List<Recipe>?): List<Recipe>? {
        var items = recipes
        if (!items.isNullOrEmpty()) {
            val currentCuisines = currentSelectedCuisines.value
            items = if (currentCuisines.isNullOrEmpty()) items
            else if (currentCuisines.contains(-1)) items
            else items.filter { currentCuisines.contains(it.cuisine) }

            val filteredRecipeIds = filteredRecipeIds.value
            items = if (filteredRecipeIds.isNullOrEmpty()) null
            else if (filteredRecipeIds.contains(ALL_UUID)) items
            else items.filter { filteredRecipeIds.contains(it.id) }
        }
        return items
    }

    fun zoomImageFromThumb(thumbView: View, imageBitmap: Bitmap) {
        largeImage = LargeImageStore(thumbView, imageBitmap, appBarState)
        zoomMode = true
        navigateLargeImageView.call()
    }

    fun getLargeImage(): LargeImageStore = largeImage

    fun getZoomMode() = zoomMode

    fun setZoomMode(mode: Boolean) {
        zoomMode = mode
    }

    fun doSnackbar(
        resId: Int? = null,
        text: CharSequence? = null,
        duration: Int = Snackbar.LENGTH_SHORT
    ) {
        //resId is a priority to text
        if (resId == null) {
            if (text != null)
                navigateSnackbar.value = SnackbarStore(text = text, duration = duration)
        } else
            navigateSnackbar.value = SnackbarStore(resId = resId, duration = duration)
    }

    fun isBitmapUpdated(id: String?): Boolean {
        if (id == null) return false

        return imageChangedTracker[UUID.fromString(id)] != null
    }

    fun getImageFromStorage(image: String) =
//        ImageStorageManager.getImageFromInternalStorage(getApplication(), image)
        ImageStorageManager.getImageFromExternalStorage(image)


    private fun saveImageToStorage(imageFileName: String, imageBitmap: Bitmap) {
//        ImageStorageManager.saveToInternalStorage(getApplication(), imageBitmap, imageFileName)
        ImageStorageManager.saveToExternalStorage(getApplication(), imageBitmap, imageFileName)
    }

    private fun deleteImageFromStorage(imageFileName: String) {
//        ImageStorageManager.deleteImageFromInternalStorage(getApplication(), imageFileName)
        ImageStorageManager.deleteImageFromExternalStorage(imageFileName)
    }

    private fun saveBitmapToFileOrDeleteOldImagesIfBitmapIsNull(
        fileName: String?,
        imageBitmap: Bitmap?,
        scope: CoroutineScope,
    ): String? {
        if (imageBitmap == null) {
            if (fileName != null) deleteImagesFiles(fileName, scope)
            return null
        }

        val tempFileName = fileName ?: getNextId().toString()

        scope.launch() {
            saveImageToStorage(tempFileName.plus(THUMB_IMAGE_POSTFIX), imageBitmap)
        }

        scope.launch(Dispatchers.IO) {
            saveImageToStorage(tempFileName, imageBitmap)
        }

        return tempFileName
    }

    private fun deleteImagesFiles(fileName: String?, scope: CoroutineScope) {
        if (fileName != null) {
            scope.launch(Dispatchers.IO) {
                deleteImageFromStorage(fileName)
                deleteImageFromStorage(fileName.plus(THUMB_IMAGE_POSTFIX))
            }
        }
    }

    private fun deleteImagesFromImageForDeleteTracker() {
        imageForDeleteTracker.keys.forEach {
            deleteImagesFiles(it, viewModelScope)
        }

        imageForDeleteTracker.clear()
    }

    private fun constructRecipeForShare(recipe: Recipe): String {
        var result = (getApplication() as Context).getString(R.string.recipe_share_recipe_text)
            .plus("\n")
            .plus((getApplication() as Context).getString(R.string.recipe_share_title_text))
            .plus("$SHARE_DIVIDER${recipe.title}\n")
            .plus((getApplication() as Context).getString(R.string.recipe_share_author_text))
            .plus("$SHARE_DIVIDER${recipe.author}\n")

        if (recipe.description.isNullOrEmpty())
            result.plus("\n")
        else {
            result.plus(
                (getApplication() as Context).getString(R.string.recipe_description_hint)
            )
                .plus("$SHARE_DIVIDER${recipe.description}\n")
        }

        val tempSteps = getStepsByRecipeId(recipe.id)?.filter { it.description.isNotEmpty() }
        if (!tempSteps.isNullOrEmpty()) {
            result = result
                .plus("\n")
                .plus((getApplication() as Context).getString(R.string.recipe_tab_title_steps))
                .plus("\n")
                .plus(
                    tempSteps.mapIndexed { index, step -> "${index + 1}$SHARE_DIVIDER${step.description}" }
                        .joinToString(separator = "\n") { it })
                .plus("\n")
        }

        val tempIngredients =
            getIngredientsByRecipeId(recipe.id)?.filter { it.description.isNotEmpty() }
        if (!tempIngredients.isNullOrEmpty()) {
            result = result
                .plus("\n")
                .plus((getApplication() as Context).getString(R.string.recipe_tab_title_ingredients))
                .plus("\n")
                .plus(tempIngredients.mapIndexed { index, ingredient -> "${index + 1}$SHARE_DIVIDER${ingredient.description}" }
                    .joinToString(separator = "\n") { it })
        }

        return result
    }
}
