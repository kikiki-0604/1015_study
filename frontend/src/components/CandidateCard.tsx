import { Candidate } from '../types'

type Props = {
  candidate: Candidate
  onComplete: (candidate: Candidate) => void
}

export default function CandidateCard({ candidate, onComplete }: Props) {
  return (
    <div style={{ border: '1px solid #ddd', borderRadius: 8, padding: 16 }}>
      <h3>{candidate.name}</h3>
      <p>{candidate.description}</p>
      <div style={{ display: 'flex', gap: 8 }}>
        <a href={candidate.recipeUrl} target="_blank" rel="noreferrer">
          <button type="button">レシピを見る</button>
        </a>
        <button type="button" onClick={() => onComplete(candidate)}>完成</button>
      </div>
    </div>
  )
}
