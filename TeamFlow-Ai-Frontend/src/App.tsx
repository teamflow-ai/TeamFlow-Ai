import { FormEvent, ReactNode, useMemo, useState } from "react";
import { Navigate, NavLink, Route, Routes, useNavigate } from "react-router-dom";
import {
  Activity, Bell, Bot, CalendarDays, CheckCircle2, ChevronRight, CircleUserRound,
  Clock3, FolderKanban, LayoutDashboard, LogOut, Menu, MessageSquareText, Plus,
  Search, Settings, Sparkles, Target, Users, X, Zap
} from "lucide-react";
import { login, register, Role, User } from "./api";

type Auth = { user: User; token: string } | null;

const projects = [
  { name: "Mobile App Redesign", client: "Product", progress: 76, tasks: "18/24", due: "Aug 12", status: "On track" },
  { name: "AI Support Assistant", client: "Innovation", progress: 58, tasks: "14/27", due: "Aug 22", status: "At risk" },
  { name: "Analytics Dashboard", client: "Growth", progress: 91, tasks: "31/34", due: "Aug 05", status: "On track" }
];

const tasks = [
  ["Finalize onboarding flow", "Mobile App Redesign", "Today", "High"],
  ["Review vector search results", "AI Support Assistant", "Tomorrow", "Medium"],
  ["QA conversion dashboard", "Analytics Dashboard", "Aug 03", "High"],
  ["Prepare sprint retrospective", "Mobile App Redesign", "Aug 04", "Low"]
];

function App() {
  const [auth, setAuth] = useState<Auth>(() => {
    const raw = localStorage.getItem("tf_user");
    return raw ? { user: JSON.parse(raw), token: localStorage.getItem("tf_access") || "" } : null;
  });


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
      <Route path="/*" element={auth ? <Shell user={auth.user} signOut={signOut} /> : <Navigate to="/login" />} />
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

function Shell({user,signOut}:{user:User;signOut:()=>void}) {
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
        <Route path="/" element={<Dashboard user={user}/>}/><Route path="/projects" element={<Projects/>}/>
        <Route path="/tasks" element={<Tasks/>}/><Route path="/team" element={<Team/>}/>
        <Route path="/ai" element={<AIPage/>}/><Route path="*" element={<ComingSoon/>}/>
      </Routes>
    </div>
  </div>;
}

function Dashboard({user}:{user:User}) {
  return <main className="page">
    <div className="page-title"><div><p className="muted">Friday, July 31</p><h1>Good evening, {user.firstName} 👋</h1><p>Here's what needs your attention today.</p></div><button className="primary"><Plus/> New project</button></div>
    <section className="stats">
      <Stat icon={<FolderKanban/>} value="8" label="Active projects" delta="+2 this month"/>
      <Stat icon={<CheckCircle2/>} value="42" label="Tasks completed" delta="+18% vs last week"/>
      <Stat icon={<Clock3/>} value="12" label="Tasks in progress" delta="4 due this week"/>
      <Stat icon={<Users/>} value="16" label="Team members" delta="2 online now"/>
    </section>
    <div className="dashboard-grid">
      <section className="panel span2"><PanelHead title="Project health" action="View all"/>
        <div className="project-list">{projects.map(p=><ProjectRow key={p.name} p={p}/>)}</div>
      </section>
      <section className="panel focus"><span className="eyebrow"><Sparkles size={14}/> AI INSIGHT</span><h3>Your sprint is trending ahead.</h3><p>At the current velocity, Mobile App Redesign may finish <b>2 days early</b>.</p><div className="focus-chart"><span style={{height:"42%"}}/><span style={{height:"55%"}}/><span style={{height:"48%"}}/><span style={{height:"68%"}}/><span style={{height:"77%"}}/><span style={{height:"92%"}}/></div><button>View sprint analysis <ChevronRight/></button></section>
      <section className="panel span2"><PanelHead title="My tasks" action="Open task board"/><div className="task-table">{tasks.map((t,i)=><div className="task-row" key={t[0]}><button className="task-check"/><div><b>{t[0]}</b><small>{t[1]}</small></div><span className={"priority "+t[3].toLowerCase()}>{t[3]}</span><time>{t[2]}</time></div>)}</div></section>
      <section className="panel"><PanelHead title="Team activity" action=""/><div className="activity-list">
        <ActivityItem initials="AR" text={<><b>Aarav</b> completed <b>API integration</b></>} time="12m"/>
        <ActivityItem initials="NK" text={<><b>Neha</b> commented on <b>Dashboard UI</b></>} time="34m"/>
        <ActivityItem initials="RV" text={<><b>Rohan</b> moved a task to Review</>} time="1h"/>
      </div></section>
    </div>
  </main>;
}

function Projects(){return <main className="page"><div className="page-title"><div><p className="muted">Workspace</p><h1>Projects</h1><p>Track delivery, progress and team ownership.</p></div><button className="primary"><Plus/> New project</button></div><div className="project-cards">{[...projects,{name:"Website Performance",client:"Engineering",progress:44,tasks:"9/21",due:"Sep 01",status:"On track"}].map(p=><div className="project-card" key={p.name}><div className="project-icon"><FolderKanban/></div><span className={"status "+(p.status==="At risk"?"risk":"")}>{p.status}</span><h3>{p.name}</h3><p>{p.client} workspace</p><div className="card-meta"><span>{p.tasks} tasks</span><span>Due {p.due}</span></div><Progress value={p.progress}/><div className="card-foot"><span>{p.progress}% complete</span><div className="avatars"><i>AR</i><i>NM</i><i>+3</i></div></div></div>)}</div></main>}

function Tasks(){return <main className="page"><div className="page-title"><div><p className="muted">Personal workspace</p><h1>My Tasks</h1><p>Focus on the work that moves projects forward.</p></div><button className="primary"><Plus/> Add task</button></div><section className="panel"><div className="task-table large">{[...tasks,["Design empty state","AI Support Assistant","Aug 07","Medium"],["Update release notes","Analytics Dashboard","Aug 08","Low"]].map(t=><div className="task-row" key={t[0]}><button className="task-check"/><div><b>{t[0]}</b><small>{t[1]}</small></div><span className={"priority "+t[3].toLowerCase()}>{t[3]}</span><time>{t[2]}</time></div>)}</div></section></main>}

function Team(){return <main className="page"><div className="page-title"><div><p className="muted">Organization</p><h1>Your Team</h1><p>People collaborating across TeamFlow AI.</p></div><button className="primary"><Plus/> Invite member</button></div><div className="people">{["Aarav Rao|Backend Engineer","Neha Kulkarni|Product Designer","Rohan Verma|Frontend Engineer","Isha Patil|QA Engineer","Kabir Shah|DevOps Engineer","Meera Joshi|Business Analyst"].map((x,i)=>{const [n,r]=x.split("|");return <div className="person" key={n}><Avatar name={n}/><h3>{n}</h3><p>{r}</p><span className={i<2?"online":"offline"}>{i<2?"Online":"Offline"}</span><button>View profile</button></div>})}</div></main>}

function AIPage(){return <main className="page ai-page"><div className="ai-hero"><div className="ai-icon"><Bot/></div><span className="eyebrow">TEAMFLOW INTELLIGENCE</span><h1>What can I help you move forward?</h1><p>Ask about project health, blockers, priorities or your team's workload.</p><div className="ai-input"><input placeholder="Ask TeamFlow AI anything..."/><button><Zap/></button></div><div className="prompts"><button>Summarize this sprint</button><button>Find delivery risks</button><button>Plan my day</button></div></div></main>}
function ComingSoon(){return <main className="page"><section className="empty"><Target/><h1>Coming soon</h1><p>This module is ready to connect when its backend service is implemented.</p></section></main>}

function NavItem({to,icon,children}:{to:string;icon:ReactNode;children:ReactNode}){return <NavLink to={to} end={to==="/"}>{icon}<span>{children}</span></NavLink>}
function Logo(){return <span className="logo"><Zap/></span>}
function Avatar({name}:{name:string}){return <span className="avatar">{name.split(" ").map(x=>x[0]).slice(0,2).join("").toUpperCase()}</span>}
function Stat({icon,value,label,delta}:{icon:ReactNode;value:string;label:string;delta:string}){return <div className="stat"><div className="stat-icon">{icon}</div><div><span className="stat-value">{value}</span><p>{label}</p><small>{delta}</small></div></div>}
function PanelHead({title,action}:{title:string;action:string}){return <div className="panel-head"><h2>{title}</h2>{action&&<button>{action}<ChevronRight/></button>}</div>}
function Progress({value}:{value:number}){return <div className="progress"><span style={{width:`${value}%`}}/></div>}
function ProjectRow({p}:{p:(typeof projects)[0]}){return <div className="project-row"><div className="project-icon"><FolderKanban/></div><div className="project-info"><b>{p.name}</b><small>{p.client} • Due {p.due}</small></div><div className="row-progress"><Progress value={p.progress}/><small>{p.progress}%</small></div><span className={"status "+(p.status==="At risk"?"risk":"")}>{p.status}</span><span className="tasks-count">{p.tasks}</span></div>}
function ActivityItem({initials,text,time}:{initials:string;text:ReactNode;time:string}){return <div className="activity-item"><span className="avatar">{initials}</span><p>{text}<small>{time} ago</small></p></div>}
export default App;