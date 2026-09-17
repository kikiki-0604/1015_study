import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { createRecord } from '../api/client'
import { Candidate } from '../types'
import { CheckIcon } from '../components/icons'

type Props = {
  candidate: Candidate | null
}

export default function CandidateDetailPage({ candidate }: Props) {
  const [recorded, setRecorded] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const navigate = useNavigate()

  if (!candidate) {
    return (
      <div className="app-shell">
        <div className="empty-state">
          <p>候補が選択されていません。食材入力からやり直してください。</p>
          <button type="button" className="btn btn-primary" onClick={() => navigate('/')}>
            入力画面に戻る
          </button>
        </div>
      </div>
    )
  }

  const handleMake = async () => {
    setError(null)
    window.open(candidate.recipeUrl, '_blank', 'noreferrer')
    try {
      const today = new Date().toISOString().slice(0, 10)
      await createRecord(candidate.name, candidate.recipeUrl, today)
      setRecorded(true)
    } catch (e) {
      setError(e instanceof Error ? e.message : '記録に失敗しました')
    }
  }

  return (
    <div className="app-shell">
      <header className="app-header">
        <h1 className="app-title">{candidate.name}</h1>
        <p className="app-tagline">{candidate.description}</p>
      </header>

      <table className="nutrition-table">
        <tbody>
          <tr>
            <th>カロリー</th>
            <td>{candidate.calories} kcal</td>
          </tr>
          <tr>
            <th>脂質</th>
            <td>{candidate.fat} g</td>
          </tr>
          <tr>
            <th>糖質</th>
            <td>{candidate.sugar} g</td>
          </tr>
          <tr>
            <th>炭水化物</th>
            <td>{candidate.carbs} g</td>
          </tr>
        </tbody>
      </table>
      <p className="nutrition-note">※AIによる1人前のおおよその概算値です</p>

      <div className="submit-row">
        <button type="button" className="btn btn-accent" onClick={handleMake} disabled={recorded}>
          <CheckIcon />
          {recorded ? '記録しました' : 'この料理を作る'}
        </button>
      </div>

      {recorded && (
        <p className="banner banner-success">
          レシピを開き、履歴に記録しました。<Link to="/history">履歴を見る</Link>
        </p>
      )}
      {error && (
        <p className="banner banner-error" role="alert">
          {error}
        </p>
      )}

      <div className="submit-row">
        <Link to="/candidates">
          <button type="button" className="btn btn-outline">
            他の候補に戻る
          </button>
        </Link>
      </div>
    </div>
  )
}
