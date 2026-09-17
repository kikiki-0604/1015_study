import { CookingRecord } from '../types'
import { ExternalLinkIcon } from './icons'

type Props = {
  record: CookingRecord
}

function formatDate(dateStr: string): string {
  const [, month, day] = dateStr.split('-')
  return `${month}/${day}`
}

export default function RecordRow({ record }: Props) {
  return (
    <li className="history-row">
      <span className="history-date">{formatDate(record.cookedOn)}</span>
      <span className="history-name">{record.dishName}</span>
      <a href={record.recipeUrl} target="_blank" rel="noreferrer">
        <button type="button" className="btn btn-outline btn-sm">
          <ExternalLinkIcon />
          レシピを見る
        </button>
      </a>
    </li>
  )
}
