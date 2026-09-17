import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import RecordRow from '../components/RecordRow'
import { BowlIcon } from '../components/icons'
import { fetchRecords } from '../api/client'
import { CookingRecord } from '../types'

export default function HistoryPage() {
  const [records, setRecords] = useState<CookingRecord[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    fetchRecords()
      .then(setRecords)
      .catch((e) => setError(e instanceof Error ? e.message : '取得に失敗しました'))
      .finally(() => setLoading(false))
  }, [])

  return (
    <div className="app-shell">
      <header className="app-header">
        <h1 className="app-title">これまでの記録</h1>
        <nav className="app-nav">
          <Link to="/">食材入力に戻る</Link>
        </nav>
      </header>

      {loading && (
        <div className="loading-row">
          <span className="spinner" />
          読み込み中...
        </div>
      )}
      {error && <p className="banner banner-error" role="alert">{error}</p>}

      {!loading && !error && records.length === 0 && (
        <div className="empty-state">
          <div className="empty-state-icon">
            <BowlIcon />
          </div>
          <p>まだ記録がありません。<br />最初の一品を完成させましょう。</p>
          <Link to="/">
            <button type="button" className="btn btn-primary" style={{ marginTop: 12 }}>
              食材を入力する
            </button>
          </Link>
        </div>
      )}

      {!loading && !error && records.length > 0 && (
        <ul className="history-list">
          {records.map((record) => (
            <RecordRow key={record.id} record={record} />
          ))}
        </ul>
      )}
    </div>
  )
}
