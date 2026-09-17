import { Candidate } from '../types'
import { CheckIcon, ExternalLinkIcon } from './icons'

type Props = {
  candidate: Candidate
  onComplete: (candidate: Candidate) => void
  completed: boolean
}

export default function CandidateCard({ candidate, onComplete, completed }: Props) {
  return (
    <div className="candidate-card">
      <h3 className="candidate-name">{candidate.name}</h3>
      <p className="candidate-description">{candidate.description}</p>
      <div className="candidate-actions">
        <a href={candidate.recipeUrl} target="_blank" rel="noreferrer">
          <button type="button" className="btn btn-outline btn-sm">
            <ExternalLinkIcon />
            レシピを見る
          </button>
        </a>
        <button
          type="button"
          className="btn btn-accent btn-sm"
          onClick={() => onComplete(candidate)}
          disabled={completed}
        >
          <CheckIcon />
          {completed ? '記録しました' : '完成'}
        </button>
      </div>
    </div>
  )
}
