import { CookingRecord } from '../types'

type Props = {
  record: CookingRecord
}

export default function RecordRow({ record }: Props) {
  return (
    <tr>
      <td>{record.cookedOn}</td>
      <td>{record.dishName}</td>
      <td>
        <a href={record.recipeUrl} target="_blank" rel="noreferrer">
          <button type="button">レシピを見る</button>
        </a>
      </td>
    </tr>
  )
}
