package models.emplishlist.db

final case class DBRecipeInfo(
    recipe: DBRecipe,
    ingredients: List[DBRecipeIngredient]
)
