import { Candidate } from '../types'

type Props = {
  candidate: Candidate
  onSelect: (candidate: Candidate) => void
}

export default function CandidateCard({ candidate, onSelect }: Props) {
  return (
    <div className="candidate-card">
      <h3 className="candidate-name">{candidate.name}</h3>
      <p className="candidate-description">{candidate.description}</p>
      <div className="candidate-actions">
        <button type="button" className="btn btn-primary btn-sm" onClick={() => onSelect(candidate)}>
          これに決める
        </button>
      </div>
    </div>
  )
}
