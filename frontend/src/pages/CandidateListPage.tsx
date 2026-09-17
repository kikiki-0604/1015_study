import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import CandidateCard from '../components/CandidateCard'
import { createRecord } from '../api/client'
import { Candidate } from '../types'

type Props = {
  candidates: Candidate[]
}

export default function CandidateListPage({ candidates }: Props) {
  const [message, setMessage] = useState<string | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [completedNames, setCompletedNames] = useState<string[]>([])
  const navigate = useNavigate()

  const handleComplete = async (candidate: Candidate) => {
    setError(null)
    try {
      const today = new Date().toISOString().slice(0, 10)
      await createRecord(candidate.name, candidate.recipeUrl, today)
      setCompletedNames((prev) => [...prev, candidate.name])
      setMessage(`「${candidate.name}」を記録しました`)
    } catch (e) {
      setError(e instanceof Error ? e.message : '記録に失敗しました')
    }
  }

  if (candidates.length === 0) {
    return (
      <div className="app-shell">
        <div className="empty-state">
          <p>候補がありません。食材入力からやり直してください。</p>
          <button type="button" className="btn btn-primary" onClick={() => navigate('/')}>
            入力画面に戻る
          </button>
        </div>
      </div>
    )
  }

  return (
    <div className="app-shell">
      <header className="app-header">
        <h1 className="app-title">今日のおすすめ</h1>
        <p className="app-tagline">気に入った一品を選んでください。</p>
      </header>

      {message && <p className="banner banner-success">{message}</p>}
      {error && <p className="banner banner-error" role="alert">{error}</p>}

      <div className="candidate-grid">
        {candidates.map((candidate) => (
          <CandidateCard
            key={candidate.name}
            candidate={candidate}
            onComplete={handleComplete}
            completed={completedNames.includes(candidate.name)}
          />
        ))}
      </div>

      <div className="submit-row">
        <button type="button" className="btn btn-outline" onClick={() => navigate('/')}>
          もう一度探す
        </button>
      </div>
    </div>
  )
}
