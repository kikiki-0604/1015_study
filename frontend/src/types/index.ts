export type Candidate = {
  name: string
  description: string
  calories: number
  fat: number
  sugar: number
  carbs: number
  recipeUrl: string
}

export type CookingRecord = {
  id: number
  dishName: string
  recipeUrl: string
  cookedOn: string
}
