import { useState, useRef, useEffect } from 'react';
import { Bot, Send, User, Sparkles } from 'lucide-react';
import { aiService } from '../../services/ai.service';
import toast from 'react-hot-toast';

export default function PolicyAssistant() {
  const [messages, setMessages] = useState([
    {
      id: 1,
      type: 'bot',
      content: 'Hello! I am your HR Policy Assistant. I am currently learning the company handbook and benefits policies. How can I help you today?',
    }
  ]);
  const [input, setInput] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const messagesEndRef = useRef(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const handleSend = async (e) => {
    e.preventDefault();
    if (!input.trim()) return;

    const userMessage = { id: Date.now(), type: 'user', content: input.trim() };
    setMessages((prev) => [...prev, userMessage]);
    setInput('');
    setIsLoading(true);

    try {
      const response = await aiService.askPolicyQuestion(userMessage.content);
      const botMessage = {
        id: Date.now() + 1,
        type: 'bot',
        content: response.answer || response.message || 'I am analyzing the company HR handbook and benefits policies for you. (RAG integration in progress...)',
      };
      setMessages((prev) => [...prev, botMessage]);
    } catch (err) {
      toast.error('Failed to connect to the Policy Assistant.');
      const errorMessage = {
        id: Date.now() + 1,
        type: 'bot',
        content: 'Sorry, I am having trouble connecting to my knowledge base right now. Please try again later.',
        isError: true
      };
      setMessages((prev) => [...prev, errorMessage]);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="page-container" style={{ display: 'flex', flexDirection: 'column', height: '100%', maxHeight: 'calc(100vh - 100px)' }}>
      <header className="page-header">
        <div className="page-title">
          <h1 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Bot className="text-primary" size={28} />
            HR Policy Assistant
          </h1>
          <p className="page-subtitle">Ask questions about company policies, benefits, and guidelines.</p>
        </div>
      </header>

      <div className="card" style={{ flex: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden', backgroundColor: '#f8f9fa' }}>
        <div style={{ flex: 1, overflowY: 'auto', padding: '1.5rem', display: 'flex', flexDirection: 'column', gap: '1rem' }}>
          {messages.map((msg) => (
            <div key={msg.id} style={{ display: 'flex', gap: '1rem', alignItems: 'flex-start', flexDirection: msg.type === 'user' ? 'row-reverse' : 'row' }}>
              <div style={{
                width: '36px', height: '36px', borderRadius: '50%', display: 'flex', alignItems: 'center', justifyContent: 'center',
                backgroundColor: msg.type === 'user' ? '#0d6efd' : '#e9ecef',
                color: msg.type === 'user' ? '#fff' : '#495057'
              }}>
                {msg.type === 'user' ? <User size={20} /> : <Sparkles size={20} />}
              </div>
              <div style={{
                maxWidth: '75%',
                padding: '1rem',
                borderRadius: '12px',
                backgroundColor: msg.type === 'user' ? '#0d6efd' : '#fff',
                color: msg.type === 'user' ? '#fff' : '#212529',
                boxShadow: '0 2px 4px rgba(0,0,0,0.05)',
                borderBottomRightRadius: msg.type === 'user' ? '4px' : '12px',
                borderTopLeftRadius: msg.type === 'bot' ? '4px' : '12px',
                border: msg.isError ? '1px solid #dc3545' : msg.type === 'bot' ? '1px solid #dee2e6' : 'none'
              }}>
                <p style={{ margin: 0, whiteSpace: 'pre-wrap', lineHeight: '1.5' }}>{msg.content}</p>
              </div>
            </div>
          ))}
          {isLoading && (
            <div style={{ display: 'flex', gap: '1rem', alignItems: 'flex-start' }}>
              <div style={{ width: '36px', height: '36px', borderRadius: '50%', backgroundColor: '#e9ecef', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#495057' }}>
                <Sparkles size={20} />
              </div>
              <div style={{ padding: '1rem', borderRadius: '12px', backgroundColor: '#fff', border: '1px solid #dee2e6', borderTopLeftRadius: '4px' }}>
                <div style={{ display: 'flex', gap: '4px' }}>
                  <div className="spinner-grow spinner-grow-sm text-secondary" role="status" style={{ width: '0.5rem', height: '0.5rem' }}></div>
                  <div className="spinner-grow spinner-grow-sm text-secondary" role="status" style={{ width: '0.5rem', height: '0.5rem', animationDelay: '0.1s' }}></div>
                  <div className="spinner-grow spinner-grow-sm text-secondary" role="status" style={{ width: '0.5rem', height: '0.5rem', animationDelay: '0.2s' }}></div>
                </div>
              </div>
            </div>
          )}
          <div ref={messagesEndRef} />
        </div>

        <div style={{ padding: '1rem', backgroundColor: '#fff', borderTop: '1px solid #dee2e6' }}>
          <form onSubmit={handleSend} style={{ display: 'flex', gap: '0.5rem' }}>
            <input
              type="text"
              className="form-control"
              placeholder="Ask about leave policy, health insurance..."
              value={input}
              onChange={(e) => setInput(e.target.value)}
              disabled={isLoading}
              style={{ borderRadius: '20px', paddingLeft: '1.5rem' }}
            />
            <button type="submit" className="btn btn-primary" disabled={isLoading || !input.trim()} style={{ borderRadius: '50%', width: '44px', height: '44px', padding: 0, display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
              <Send size={18} />
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}
