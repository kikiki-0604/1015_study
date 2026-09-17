import { useState } from 'react'
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import IngredientInputPage from './pages/IngredientInputPage'
import CandidateListPage from './pages/CandidateListPage'
import HistoryPage from './pages/HistoryPage'
import { Candidate } from './types'

export default function App() {
  const [candidates, setCandidates] = useState<Candidate[]>([])

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<IngredientInputPage onSuggestions={setCandidates} />} />
        <Route path="/candidates" element={<CandidateListPage candidates={candidates} />} />
        <Route path="/history" element={<HistoryPage />} />
      </Routes>
    </BrowserRouter>
  )
}
