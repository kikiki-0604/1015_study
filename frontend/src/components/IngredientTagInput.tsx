import { useState } from 'react'

type Props = {
  ingredients: string[]
  onChange: (ingredients: string[]) => void
}

export default function IngredientTagInput({ ingredients, onChange }: Props) {
  const [text, setText] = useState('')

  const addIngredient = () => {
    const trimmed = text.trim()
    if (trimmed.length === 0) return
    onChange([...ingredients, trimmed])
    setText('')
  }

  const removeIngredient = (index: number) => {
    onChange(ingredients.filter((_, i) => i !== index))
  }

  return (
    <div>
      <div className="ingredient-row">
        <input
          className="ingredient-field"
          value={text}
          onChange={(e) => setText(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === 'Enter') {
              e.preventDefault()
              addIngredient()
            }
          }}
          placeholder="例: 卵、玉ねぎ、豚こま"
          aria-label="食材を入力"
        />
        <button type="button" className="btn btn-outline" onClick={addIngredient}>
          追加
        </button>
      </div>
      {ingredients.length > 0 && (
        <div className="tag-list">
          {ingredients.map((ingredient, index) => (
            <span className="tag-chip" key={`${ingredient}-${index}`}>
              {ingredient}
              <button
                type="button"
                className="tag-chip-remove"
                onClick={() => removeIngredient(index)}
                aria-label={`${ingredient}を削除`}
              >
                ×
              </button>
            </span>
          ))}
        </div>
      )}
    </div>
  )
}
