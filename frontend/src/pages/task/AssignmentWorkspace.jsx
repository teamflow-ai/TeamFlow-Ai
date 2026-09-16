import { useEffect, useState } from 'react';
import { Sparkles, AlertTriangle, UserCheck, Clock, CheckCircle2, AlertCircle, Calendar } from 'lucide-react';
import Avatar from '../../components/common/Avatar';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import { taskService } from '../../services/task.service';
import toast from 'react-hot-toast';

export default function AssignmentWorkspace({ taskId, currentAssigneeId, onAssigned }) {
  const [candidates, setCandidates] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [assigning, setAssigning] = useState(false);

  useEffect(() => {
    loadCandidates();
  }, [taskId]);

  const loadCandidates = async () => {
    setLoading(true);
    try {
      const data = await taskService.candidates(taskId);
      setCandidates(data);
    } catch (err) {
      toast.error(err.message || 'Failed to load assignment candidates');
    } finally {
      setLoading(false);
    }
  };

  const handleAssign = async (candidate, mode = 'MANUAL') => {
    setAssigning(true);
    try {
      await taskService.assign(taskId, { assigneeId: candidate.employeeId, mode });
      toast.success(`Task assigned to ${candidate.employeeName}`);
      if (onAssigned) onAssigned();
    } catch (err) {
      toast.error(err.message || 'Failed to assign task');
    } finally {
      setAssigning(false);
    }
  };

  if (loading) {
    return <LoadingSpinner size="sm" label="Loading assignment workspace..." />;
  }

  const filteredCandidates = candidates.filter(c => 
    c.employeeName.toLowerCase().includes(search.toLowerCase())
  );

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
      <div style={{ position: 'relative' }}>
        <input 
          type="text" 
          className="form-control" 
          placeholder="Search team members by name..." 
          value={search}
          onChange={e => setSearch(e.target.value)}
          disabled={assigning}
        />
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', maxHeight: '500px', overflowY: 'auto', paddingRight: '8px' }}>
        {filteredCandidates.length === 0 && (
          <p className="text-muted-c" style={{ textAlign: 'center', padding: '20px 0', fontSize: '0.9rem' }}>
            No eligible team members found.
          </p>
        )}

        {filteredCandidates.map(candidate => {
          const isSelected = candidate.employeeId === currentAssigneeId;
          const isOverloaded = candidate.riskBand === 'HIGH' || candidate.riskBand === 'EXTREME';
          const isRecommended = candidate.isRecommended;

          return (
            <div 
              key={candidate.employeeId} 
              style={{
                border: `1px solid ${isSelected ? 'var(--color-primary)' : isRecommended ? 'var(--color-accent)' : 'var(--color-border)'}`,
                borderRadius: '8px',
                padding: '16px',
                backgroundColor: isSelected ? 'rgba(59, 130, 246, 0.05)' : isRecommended ? 'rgba(99, 102, 241, 0.05)' : 'white',
                position: 'relative'
              }}
            >
              {isRecommended && !isSelected && (
                <div style={{ position: 'absolute', top: -10, right: 16, backgroundColor: 'var(--color-accent)', color: 'white', fontSize: '0.7rem', padding: '4px 8px', borderRadius: '12px', display: 'flex', alignItems: 'center', gap: '4px', fontWeight: 600 }}>
                  <Sparkles size={12} /> BEST MATCH
                </div>
              )}
              
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
                  <Avatar name={candidate.employeeName} />
                  <div>
                    <div style={{ fontWeight: 600, fontSize: '1rem', display: 'flex', alignItems: 'center', gap: '8px' }}>
                      {candidate.employeeName}
                      {candidate.onLeaveToday && (
                        <span className="badge badge-danger" style={{ fontSize: '0.65rem' }}>ON LEAVE</span>
                      )}
                      {isOverloaded && (
                        <span className="badge badge-warning" style={{ fontSize: '0.65rem' }}>OVERLOADED</span>
                      )}
                    </div>
                    
                    <div style={{ fontSize: '0.75rem', color: 'var(--color-text-muted)', display: 'flex', alignItems: 'center', gap: '12px', marginTop: '6px' }}>
                      <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                        <Clock size={12} /> {candidate.utilizationPercent.toFixed(0)}% Utilized
                      </span>
                      <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                        <CheckCircle2 size={12} /> {candidate.activeTaskCount} Active
                      </span>
                      <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                        <AlertCircle size={12} /> {candidate.overdueTaskCount} Overdue
                      </span>
                      <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                        <Calendar size={12} /> {candidate.totalAllocatedHours || 0}h Allocated Project Capacity
                      </span>
                    </div>
                  </div>
                </div>

                <button 
                  className={`btn btn-sm ${isSelected ? 'btn-secondary' : isRecommended ? 'btn-primary' : 'btn-ghost'}`}
                  onClick={() => handleAssign(candidate, isRecommended ? 'SMART' : 'MANUAL')}
                  disabled={assigning || isSelected || candidate.onLeaveToday}
                >
                  {isSelected ? <><UserCheck size={14}/> Assigned</> : 'Assign'}
                </button>
              </div>

              {candidate.skills && candidate.skills.length > 0 && (
                <div style={{ marginTop: '12px', display: 'flex', flexWrap: 'wrap', gap: '6px' }}>
                  {candidate.skills.map(skill => (
                    <span key={skill} style={{ fontSize: '0.65rem', padding: '2px 6px', backgroundColor: 'var(--color-bg-alt)', borderRadius: '4px', border: '1px solid var(--color-border)' }}>
                      {skill}
                    </span>
                  ))}
                </div>
              )}

              {isRecommended && candidate.recommendationReasons && (
                <div style={{ marginTop: '14px', paddingTop: '10px', borderTop: '1px dashed var(--color-border)' }}>
                  <div style={{ fontSize: '0.75rem', fontWeight: 600, color: 'var(--color-accent)', marginBottom: '4px', display: 'flex', alignItems: 'center', gap: '4px' }}>
                    <Sparkles size={12} /> AI Recommendation Reason
                  </div>
                  <ul style={{ margin: 0, paddingLeft: '16px', fontSize: '0.75rem', color: 'var(--color-text-muted)', lineHeight: 1.5 }}>
                    {candidate.recommendationReasons.map((r, i) => (
                      <li key={i}>{r}</li>
                    ))}
                  </ul>
                </div>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}
