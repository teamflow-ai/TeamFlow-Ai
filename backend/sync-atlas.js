const { MongoClient } = require('mongodb');
const mysql = require('mysql2/promise');

async function run() {
  const mysqlConn = await mysql.createConnection({
    host: 'localhost',
    user: 'root',
    password: 'cdac',
    database: 'teamflow_identity'
  });
  
  const [employees] = await mysqlConn.execute('SELECT * FROM employees');
  
  const uri = "mongodb+srv://purveshdwd2000:Purvesh123@cluster0.o56v5.mongodb.net/teamflow_ai?retryWrites=true&w=majority&appName=Cluster0";
  const client = new MongoClient(uri);
  await client.connect();
  const db = client.db('teamflow_ai');
  const coll = db.collection('employee_profiles');
  
  for (let emp of employees) {
    const doc = {
      _id: emp.id,
      _class: 'com.teamflow.ai.ai.document.EmployeeProfile',
      active: true, // Force active to true so recommend works
      departmentId: emp.department_id || null,
      email: emp.work_email,
      fullName: `${emp.first_name} ${emp.last_name}`,
      skills: [],
      weeklyCapacityHours: emp.weekly_capacity_hours || 40
    };
    await coll.updateOne({ _id: emp.id }, { $set: doc }, { upsert: true });
    console.log('Synced to Atlas:', doc.fullName);
  }
  
  await client.close();
  await mysqlConn.end();
}

run();
