import { FormEvent, ReactNode, useEffect, useMemo, useState } from "react";
import { Navigate, NavLink, Route, Routes, useNavigate } from "react-router-dom";
import {
  Activity, Bell, Bot, CalendarDays, CheckCircle2, ChevronRight, CircleUserRound,
  Clock3, FolderKanban, LayoutDashboard, LogOut, Menu, MessageSquareText, Plus,
  Search, Settings, Sparkles, Target, Trash2, Users, X, Zap
} from "lucide-react";
import { login, register, Role, User } from "./api";

type Auth = { user: User; token: string } | null;

const projects = [
  { name: "Mobile App Redesign", client: "Product", progress: 76, tasks: "18/24", due: "Aug 12", status: "On track" },
  { name: "AI Support Assistant", client: "Innovation", progress: 58, tasks: "14/27", due: "Aug 22", status: "At risk" },
  { name: "Analytics Dashboard", client: "Growth", progress: 91, tasks: "31/34", due: "Aug 05", status: "On track" }
];

type Priority = "High" | "Medium" | "Low";
type Task = { id: string; title: string; project: string; due: string; priority: Priority; done: boolean };

const defaultTasks: Task[] = [
  { id: "t1", title: "Finalize onboarding flow", project: "Mobile App Redesign", due: "Today", priority: "High", done: false },
  { id: "t2", title: "Review vector search results", project: "AI Support Assistant", due: "Tomorrow", priority: "Medium", done: false },
  { id: "t3", title: "QA conversion dashboard", project: "Analytics Dashboard", due: "Aug 03", priority: "High", done: false },
  { id: "t4", title: "Prepare sprint retrospective", project: "Mobile App Redesign", due: "Aug 04", priority: "Low", done: true }
];

function useTasks() {
  const [tasks, setTasks] = useState<Task[]>(() => {
    const raw = localStorage.getItem("tf_tasks");
    return raw ? JSON.parse(raw) : defaultTasks;
  });
  useEffect(() => { localStorage.setItem("tf_tasks", JSON.stringify(tasks)); }, [tasks]);

  const addTask = (t: Omit<Task, "id" | "done">) =>
    setTasks(prev => [{ ...t, id: `t${Date.now()}`, done: false }, ...prev]);
  const toggleTask = (id: string) =>
    setTasks(prev => prev.map(t => t.id === id ? { ...t, done: !t.done } : t));
  const deleteTask = (id: string) =>
    setTasks(prev => prev.filter(t => t.id !== id));

  return { tasks, addTask, toggleTask, deleteTask };
}

function App() {
  const [auth, setAuth] = useState<Auth>(() => {
    const raw = localStorage.getItem("tf_user");
    return raw ? { user: JSON.parse(raw), token: localStorage.getItem("tf_access") || "" } : null;
  });
  const taskStore = useTasks();

  const signIn = (user: User, token: string) => {
    localStorage.setItem("tf_user", JSON.stringify(user));
    localStorage.setItem("tf_access", token);
    setAuth({ user, token });
  };
  const signOut = () => {
    localStorage.removeItem("tf_user"); localStorage.removeItem("tf_access"); localStorage.removeItem("tf_refresh");
    setAuth(null);
  };

  return (
    <Routes>
      <Route path="/login" element={auth ? <Navigate to="/" /> : <Login onAuth={signIn} />} />
      <Route path="/register" element={auth ? <Navigate to="/" /> : <Register onAuth={signIn} />} />
      <Route path="/*" element={auth ? <Shell user={auth.user} signOut={signOut} taskStore={taskStore} /> : <Navigate to="/login" />} />
    </Routes>
  );
}

function Login({ onAuth }: { onAuth: (u: User, t: string) => void }) {
  const nav = useNavigate();
  const [email, setEmail] = useState("manager@teamflow.ai");
  const [password, setPassword] = useState("teamflow123");
  const [busy, setBusy] = useState(false);
  const submit = async (e: FormEvent) => {
    e.preventDefault(); setBusy(true);
    const res = await login(email, password);
    localStorage.setItem("tf_refresh", res.refreshToken); onAuth(res.user, res.accessToken); setBusy(false);
  };
  return <AuthPage>
    <div className="auth-card">
      <div className="brand big"><Logo /> <span>TeamFlow <b>AI</b></span></div>
      <h1>Welcome back</h1><p>Sign in to keep your team moving.</p>
      <form onSubmit={submit}>
        <label>Work email</label><input type="email" value={email} onChange={e=>setEmail(e.target.value)} required />
        <label>Password</label><input type="password" value={password} onChange={e=>setPassword(e.target.value)} minLength={8} required />
        <div className="form-row"><label className="check"><input type="checkbox" /> Remember me</label><a>Forgot password?</a></div>
        <button className="primary full" disabled={busy}>{busy ? "Signing in..." : "Sign in"} <ChevronRight size={17}/></button>
      </form>
      <div className="demo-note"><Sparkles size={16}/> Backend auth controllers are not in the uploaded ZIP, so unavailable endpoints use demo mode.</div>
      <p className="center">New to TeamFlow? <button className="link-btn" onClick={()=>nav("/register")}>Create account</button></p>
    </div>
  </AuthPage>;
}

function Register({ onAuth }: { onAuth: (u: User, t: string) => void }) {
  const nav = useNavigate(); const [busy, setBusy] = useState(false);
  const [form, setForm] = useState({firstName:"",lastName:"",email:"",password:"",designation:"",role:"EMPLOYEE" as Role});
  const submit = async (e: FormEvent) => {
    e.preventDefault(); setBusy(true); const res = await register(form);
    localStorage.setItem("tf_refresh", res.refreshToken); onAuth(res.user,res.accessToken); setBusy(false);
  };
  return <AuthPage><div className="auth-card wide">
    <div className="brand big"><Logo/><span>TeamFlow <b>AI</b></span></div>
    <h1>Create your workspace account</h1><p>Join your team and start collaborating.</p>
    <form onSubmit={submit}>
      <div className="two"><div><label>First name</label><input required onChange={e=>setForm({...form,firstName:e.target.value})}/></div>
      <div><label>Last name</label><input required onChange={e=>setForm({...form,lastName:e.target.value})}/></div></div>
      <label>Email</label><input type="email" required onChange={e=>setForm({...form,email:e.target.value})}/>
      <div className="two"><div><label>Designation</label><input placeholder="Software Engineer" onChange={e=>setForm({...form,designation:e.target.value})}/></div>
      <div><label>Role</label><select value={form.role} onChange={e=>setForm({...form,role:e.target.value as Role})}><option>EMPLOYEE</option><option>TEAM_LEAD</option><option>PROJECT_MANAGER</option><option>ORGANIZATION_ADMIN</option></select></div></div>
      <label>Password</label><input type="password" minLength={8} required onChange={e=>setForm({...form,password:e.target.value})}/>
      <button className="primary full" disabled={busy}>{busy?"Creating...":"Create account"} <ChevronRight size={17}/></button>
    </form>
    <p className="center">Already registered? <button className="link-btn" onClick={()=>nav("/login")}>Sign in</button></p>
  </div></AuthPage>;
}

function AuthPage({children}:{children:ReactNode}) {
  return <main className="auth-page"><section className="auth-visual">
    <div className="orb one"/><div className="orb two"/>
    <div className="hero-copy"><span className="eyebrow"><Zap size={14}/> WORK SMARTER, TOGETHER</span>
      <h2>Turn teamwork into<br/><em>momentum.</em></h2>
      <p>Plan projects, align people, automate the busywork and let AI surface what matters next.</p>
      <div className="mini-board"><div><CheckCircle2/><span><b>12 tasks completed</b><small>This sprint</small></span></div><strong>+24%</strong></div>
    </div>
  </section><section className="auth-form">{children}</section></main>;
}

type TaskStore = { tasks: Task[]; addTask: (t: Omit<Task,"id"|"done">)=>void; toggleTask: (id:string)=>void; deleteTask: (id:string)=>void };

function Shell({user,signOut,taskStore}:{user:User;signOut:()=>void;taskStore:TaskStore}) {
  const [open,setOpen]=useState(false);
  return <div className="app">
    <aside className={open?"sidebar open":"sidebar"}>
      <div className="brand"><Logo/><span>TeamFlow <b>AI</b></span><button className="mobile-close" onClick={()=>setOpen(false)}><X/></button></div>
      <nav>
        <NavItem to="/" icon={<LayoutDashboard/>}>Overview</NavItem>
        <NavItem to="/projects" icon={<FolderKanban/>}>Projects</NavItem>
        <NavItem to="/tasks" icon={<CheckCircle2/>}>My Tasks</NavItem>
        <NavItem to="/team" icon={<Users/>}>Team</NavItem>
        <NavItem to="/calendar" icon={<CalendarDays/>}>Calendar</NavItem>
        <div className="nav-label">WORKSPACE</div>
        <NavItem to="/ai" icon={<Bot/>}>AI Assistant <span className="beta">BETA</span></NavItem>
        <NavItem to="/settings" icon={<Settings/>}>Settings</NavItem>
      </nav>
      <div className="sidebar-bottom">
        <div className="upgrade"><Sparkles/><b>TeamFlow Pro</b><small>Unlock AI planning & insights.</small><button>Explore Pro</button></div>
        <button className="profile" onClick={signOut}><Avatar name={user.firstName}/><span><b>{user.firstName} {user.lastName}</b><small>{user.designation || user.role}</small></span><LogOut size={17}/></button>
      </div>
    </aside>
    <div className="content">
      <header><button className="menu" onClick={()=>setOpen(true)}><Menu/></button><div className="search"><Search/><input placeholder="Search projects, tasks, people..."/><kbd>⌘ K</kbd></div>
        <div className="header-actions"><button><MessageSquareText/></button><button className="bell"><Bell/><i/></button><Avatar name={user.firstName}/></div></header>
      <Routes>
        <Route path="/" element={<Dashboard user={user} taskStore={taskStore}/>}/><Route path="/projects" element={<Projects/>}/>
        <Route path="/tasks" element={<Tasks taskStore={taskStore}/>}/><Route path="/team" element={<Team/>}/>
        <Route path="/ai" element={<AIPage/>}/><Route path="*" element={<ComingSoon/>}/>
      </Routes>
    </div>
  </div>;
}

function Dashboard({user,taskStore}:{user:User;taskStore:TaskStore}) {
  const {tasks,toggleTask}=taskStore;
  const completed = tasks.filter(t=>t.done).length;
  const inProgress = tasks.length - completed;
  const [showAdd,setShowAdd]=useState(false);
  return <main className="page">
    <div className="page-title"><div><p className="muted">Friday, July 31</p><h1>Good evening, {user.firstName} 👋</h1><p>Here's what needs your attention today.</p></div><button className="primary"><Plus/> New project</button></div>
    <section className="stats">
      <Stat icon={<FolderKanban/>} value="8" label="Active projects" delta="+2 this month"/>
      <Stat icon={<CheckCircle2/>} value={String(completed)} label="Tasks completed" delta={`${completed} of ${tasks.length} total`}/>
      <Stat icon={<Clock3/>} value={String(inProgress)} label="Tasks in progress" delta="Updated live"/>
      <Stat icon={<Users/>} value="16" label="Team members" delta="2 online now"/>
    </section>
    <div className="dashboard-grid">
      <section className="panel span2"><PanelHead title="Project health" action="View all"/>
        <div className="project-list">{projects.map(p=><ProjectRow key={p.name} p={p}/>)}</div>
      </section>
      <section className="panel focus"><span className="eyebrow"><Sparkles size={14}/> AI INSIGHT</span><h3>Your sprint is trending ahead.</h3><p>At the current velocity, Mobile App Redesign may finish <b>2 days early</b>.</p><div className="focus-chart"><span style={{height:"42%"}}/><span style={{height:"55%"}}/><span style={{height:"48%"}}/><span style={{height:"68%"}}/><span style={{height:"77%"}}/><span style={{height:"92%"}}/></div><button>View sprint analysis <ChevronRight/></button></section>
      <section className="panel span2"><PanelHead title="My tasks" action="Add task" onAction={()=>setShowAdd(true)}/><div className="task-table">{tasks.slice(0,4).map(t=><TaskRow key={t.id} t={t} onToggle={()=>toggleTask(t.id)}/>)}</div></section>
      <section className="panel"><PanelHead title="Team activity" action=""/><div className="activity-list">
        <ActivityItem initials="AR" text={<><b>Aarav</b> completed <b>API integration</b></>} time="12m"/>
        <ActivityItem initials="NK" text={<><b>Neha</b> commented on <b>Dashboard UI</b></>} time="34m"/>
        <ActivityItem initials="RV" text={<><b>Rohan</b> moved a task to Review</>} time="1h"/>
      </div></section>
    </div>
    {showAdd && <AddTaskModal onClose={()=>setShowAdd(false)} onAdd={taskStore.addTask}/>}
  </main>;
}

function Projects(){return <main className="page"><div className="page-title"><div><p className="muted">Workspace</p><h1>Projects</h1><p>Track delivery, progress and team ownership.</p></div><button className="primary"><Plus/> New project</button></div><div className="project-cards">{[...projects,{name:"Website Performance",client:"Engineering",progress:44,tasks:"9/21",due:"Sep 01",status:"On track"}].map(p=><div className="project-card" key={p.name}><div className="project-icon"><FolderKanban/></div><span className={"status "+(p.status==="At risk"?"risk":"")}>{p.status}</span><h3>{p.name}</h3><p>{p.client} workspace</p><div className="card-meta"><span>{p.tasks} tasks</span><span>Due {p.due}</span></div><Progress value={p.progress}/><div className="card-foot"><span>{p.progress}% complete</span><div className="avatars"><i>AR</i><i>NM</i><i>+3</i></div></div></div>)}</div></main>}

function Tasks({taskStore}:{taskStore:TaskStore}){
  const {tasks,toggleTask,deleteTask,addTask}=taskStore;
  const [showAdd,setShowAdd]=useState(false);
  const [filter,setFilter]=useState<"all"|"open"|"done">("all");
  const visible = tasks.filter(t=>filter==="all"?true:filter==="done"?t.done:!t.done);
  return <main className="page">
    <div className="page-title"><div><p className="muted">Personal workspace</p><h1>My Tasks</h1><p>Focus on the work that moves projects forward.</p></div><button className="primary" onClick={()=>setShowAdd(true)}><Plus/> Add task</button></div>
    <div className="task-filters">
      <button className={filter==="all"?"active":""} onClick={()=>setFilter("all")}>All ({tasks.length})</button>
      <button className={filter==="open"?"active":""} onClick={()=>setFilter("open")}>Open ({tasks.filter(t=>!t.done).length})</button>
      <button className={filter==="done"?"active":""} onClick={()=>setFilter("done")}>Done ({tasks.filter(t=>t.done).length})</button>
    </div>
    <section className="panel">
      {visible.length===0
        ? <p className="muted" style={{padding:"20px 4px"}}>No tasks here.</p>
        : <div className="task-table large">{visible.map(t=><TaskRow key={t.id} t={t} onToggle={()=>toggleTask(t.id)} onDelete={()=>deleteTask(t.id)}/>)}</div>}
    </section>
    {showAdd && <AddTaskModal onClose={()=>setShowAdd(false)} onAdd={addTask}/>}
  </main>;
}

function TaskRow({t,onToggle,onDelete}:{t:Task;onToggle:()=>void;onDelete?:()=>void}){
  return <div className={"task-row"+(t.done?" done":"")}>
    <button className={"task-check"+(t.done?" checked":"")} onClick={onToggle} aria-label="Toggle complete">{t.done && <CheckCircle2 size={13}/>}</button>
    <div><b>{t.title}</b><small>{t.project}</small></div>
    <span className={"priority "+t.priority.toLowerCase()}>{t.priority}</span>
    <time>{t.due}</time>
    {onDelete && <button className="task-delete" onClick={onDelete} aria-label="Delete task"><Trash2 size={14}/></button>}
  </div>;
}

function AddTaskModal({onClose,onAdd}:{onClose:()=>void;onAdd:(t:Omit<Task,"id"|"done">)=>void}){
  const [title,setTitle]=useState("");
  const [project,setProject]=useState(projects[0].name);
  const [due,setDue]=useState("");
  const [priority,setPriority]=useState<Priority>("Medium");
  const submit=(e:FormEvent)=>{
    e.preventDefault();
    if(!title.trim()) return;
    onAdd({title:title.trim(),project,due:due.trim()||"No due date",priority});
    onClose();
  };
  return <div className="modal-overlay" onClick={onClose}>
    <div className="modal-card" onClick={e=>e.stopPropagation()}>
      <div className="modal-head"><h2>Add task</h2><button className="link-btn" onClick={onClose}><X size={18}/></button></div>
      <form onSubmit={submit}>
        <label>Task name</label>
        <input autoFocus value={title} onChange={e=>setTitle(e.target.value)} placeholder="e.g. Write release notes" required/>
        <div className="two">
          <div><label>Project</label>
            <select value={project} onChange={e=>setProject(e.target.value)}>
              {projects.map(p=><option key={p.name} value={p.name}>{p.name}</option>)}
            </select>
          </div>
          <div><label>Priority</label>
            <select value={priority} onChange={e=>setPriority(e.target.value as Priority)}>
              <option>High</option><option>Medium</option><option>Low</option>
            </select>
          </div>
        </div>
        <label>Due</label>
        <input value={due} onChange={e=>setDue(e.target.value)} placeholder="e.g. Aug 10, Tomorrow"/>
        <button className="primary full">Add task</button>
      </form>
    </div>
  </div>;
}

function Team(){return <main className="page"><div className="page-title"><div><p className="muted">Organization</p><h1>Your Team</h1><p>People collaborating across TeamFlow AI.</p></div><button className="primary"><Plus/> Invite member</button></div><div className="people">{["Aarav Rao|Backend Engineer","Neha Kulkarni|Product Designer","Rohan Verma|Frontend Engineer","Isha Patil|QA Engineer","Kabir Shah|DevOps Engineer","Meera Joshi|Business Analyst"].map((x,i)=>{const [n,r]=x.split("|");return <div className="person" key={n}><Avatar name={n}/><h3>{n}</h3><p>{r}</p><span className={i<2?"online":"offline"}>{i<2?"Online":"Offline"}</span><button>View profile</button></div>})}</div></main>}

function AIPage(){return <main className="page ai-page"><div className="ai-hero"><div className="ai-icon"><Bot/></div><span className="eyebrow">TEAMFLOW INTELLIGENCE</span><h1>What can I help you move forward?</h1><p>Ask about project health, blockers, priorities or your team's workload.</p><div className="ai-input"><input placeholder="Ask TeamFlow AI anything..."/><button><Zap/></button></div><div className="prompts"><button>Summarize this sprint</button><button>Find delivery risks</button><button>Plan my day</button></div></div></main>}
function ComingSoon(){return <main className="page"><section className="empty"><Target/><h1>Coming soon</h1><p>This module is ready to connect when its backend service is implemented.</p></section></main>}

function NavItem({to,icon,children}:{to:string;icon:ReactNode;children:ReactNode}){return <NavLink to={to} end={to==="/"}>{icon}<span>{children}</span></NavLink>}
function Logo(){return <span className="logo"><Zap/></span>}
function Avatar({name}:{name:string}){return <span className="avatar">{name.split(" ").map(x=>x[0]).slice(0,2).join("").toUpperCase()}</span>}
function Stat({icon,value,label,delta}:{icon:ReactNode;value:string;label:string;delta:string}){return <div className="stat"><div className="stat-icon">{icon}</div><div><span className="stat-value">{value}</span><p>{label}</p><small>{delta}</small></div></div>}
function PanelHead({title,action,onAction}:{title:string;action:string;onAction?:()=>void}){return <div className="panel-head"><h2>{title}</h2>{action&&<button onClick={onAction}>{action}<ChevronRight/></button>}</div>}
function Progress({value}:{value:number}){return <div className="progress"><span style={{width:`${value}%`}}/></div>}
function ProjectRow({p}:{p:(typeof projects)[0]}){return <div className="project-row"><div className="project-icon"><FolderKanban/></div><div className="project-info"><b>{p.name}</b><small>{p.client} • Due {p.due}</small></div><div className="row-progress"><Progress value={p.progress}/><small>{p.progress}%</small></div><span className={"status "+(p.status==="At risk"?"risk":"")}>{p.status}</span><span className="tasks-count">{p.tasks}</span></div>}
function ActivityItem({initials,text,time}:{initials:string;text:ReactNode;time:string}){return <div className="activity-item"><span className="avatar">{initials}</span><p>{text}<small>{time} ago</small></p></div>}

export default App;
