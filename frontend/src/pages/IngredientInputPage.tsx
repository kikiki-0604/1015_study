import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import IngredientTagInput from '../components/IngredientTagInput'
import { fetchSuggestions } from '../api/client'
import { Candidate } from '../types'

type Props = {
  onSuggestions: (candidates: Candidate[]) => void
}

export default function IngredientInputPage({ onSuggestions }: Props) {
  const [ingredients, setIngredients] = useState<string[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const navigate = useNavigate()

  const handleSubmit = async () => {
    if (ingredients.length === 0) {
      setError('食材を1つ以上入力してください')
      return
    }
    setLoading(true)
    setError(null)
    try {
      const { candidates } = await fetchSuggestions(ingredients)
      onSuggestions(candidates)
      navigate('/candidates')
    } catch (e) {
      setError(e instanceof Error ? e.message : '候補の取得に失敗しました')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div>
      <h1>献立メーカー</h1>
      <nav><Link to="/history">履歴を見る</Link></nav>
      <p>冷蔵庫にある食材を入力してください。</p>
      <IngredientTagInput ingredients={ingredients} onChange={setIngredients} />
      <button type="button" onClick={handleSubmit} disabled={loading} style={{ marginTop: 16 }}>
        {loading ? '提案を取得中...' : '候補を提案してもらう'}
      </button>
      {error && <p role="alert" style={{ color: 'red' }}>{error}</p>}
    </div>
  )
}
