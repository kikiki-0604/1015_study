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
  const navigate = useNavigate()

  const handleComplete = async (candidate: Candidate) => {
    setError(null)
    try {
      const today = new Date().toISOString().slice(0, 10)
      await createRecord(candidate.name, candidate.recipeUrl, today)
      setMessage(`${candidate.name}を記録しました`)
    } catch (e) {
      setError(e instanceof Error ? e.message : '記録に失敗しました')
    }
  }

  if (candidates.length === 0) {
    return (
      <div>
        <p>候補がありません。食材入力からやり直してください。</p>
        <button type="button" onClick={() => navigate('/')}>入力画面に戻る</button>
      </div>
    )
  }

  return (
    <div>
      <h1>候補一覧</h1>
      {message && <p>{message}</p>}
      {error && <p role="alert" style={{ color: 'red' }}>{error}</p>}
      <div style={{ display: 'grid', gap: 16 }}>
        {candidates.map((candidate) => (
          <CandidateCard key={candidate.name} candidate={candidate} onComplete={handleComplete} />
        ))}
      </div>
      <button type="button" onClick={() => navigate('/')} style={{ marginTop: 16 }}>
        もう一度提案してもらう
      </button>
    </div>
  )
}
