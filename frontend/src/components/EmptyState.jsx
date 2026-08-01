import { Inbox } from 'lucide-react';

export default function EmptyState({ icon: Icon = Inbox, title, message, action }) {
  return (
    <div className="empty-state fade-in">
      <div className="empty-state-icon">
        <Icon size={28} />
      </div>
      {title && <h3>{title}</h3>}
      {message && <p className="empty-state-message">{message}</p>}
      {action}
    </div>
  );
}
