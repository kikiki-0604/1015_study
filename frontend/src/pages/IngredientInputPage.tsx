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
    <div className="app-shell">
      <header className="app-header">
        <h1 className="app-title">献立メーカー</h1>
        <p className="app-tagline">冷蔵庫の中身から、今日の一品を。</p>
        <nav className="app-nav">
          <Link to="/history">これまでの記録を見る</Link>
        </nav>
      </header>

      <section>
        <h2 className="section-heading">今日ある食材は?</h2>
        <p className="section-lead">思いついたものから入力してください。Enterでも追加できます。</p>
        <IngredientTagInput ingredients={ingredients} onChange={setIngredients} />
      </section>

      <div className="submit-row">
        <button type="button" className="btn btn-primary" onClick={handleSubmit} disabled={loading}>
          {loading ? '考え中...' : '候補を探す'}
        </button>
        {loading && (
          <div className="loading-row">
            <span className="spinner" />
            AIが献立を考えています
          </div>
        )}
      </div>

      {error && <p className="banner banner-error" role="alert">{error}</p>}
    </div>
  )
}
