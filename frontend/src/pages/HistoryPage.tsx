import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import RecordRow from '../components/RecordRow'
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
    <div>
      <h1>履歴</h1>
      <nav><Link to="/">食材入力に戻る</Link></nav>
      {loading && <p>読み込み中...</p>}
      {error && <p role="alert" style={{ color: 'red' }}>{error}</p>}
      {!loading && !error && records.length === 0 && <p>まだ記録がありません</p>}
      {!loading && !error && records.length > 0 && (
        <table>
          <thead>
            <tr><th>日付</th><th>料理名</th><th></th></tr>
          </thead>
          <tbody>
            {records.map((record) => <RecordRow key={record.id} record={record} />)}
          </tbody>
        </table>
      )}
    </div>
  )
}
