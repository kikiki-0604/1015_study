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
      <div style={{ display: 'flex', gap: 8 }}>
        <input
          value={text}
          onChange={(e) => setText(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === 'Enter') {
              e.preventDefault()
              addIngredient()
            }
          }}
          placeholder="例: 卵"
        />
        <button type="button" onClick={addIngredient}>追加</button>
      </div>
      <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', marginTop: 8 }}>
        {ingredients.map((ingredient, index) => (
          <span key={`${ingredient}-${index}`} style={{ border: '1px solid #ccc', borderRadius: 16, padding: '4px 12px' }}>
            {ingredient}
            <button type="button" onClick={() => removeIngredient(index)} style={{ marginLeft: 8 }}>×</button>
          </span>
        ))}
      </div>
    </div>
  )
}
