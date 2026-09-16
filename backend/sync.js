const { MongoClient } = require('mongodb');
const mysql = require('mysql2/promise');

async function sync() {
  try {
    const mysqlConn = await mysql.createConnection({
      host: 'localhost',
      user: 'root',
      password: 'cdac',
      database: 'teamflow_identity'
    });

    const [employees] = await mysqlConn.execute('SELECT * FROM employees');
    console.log(`Found ${employees.length} employees in MySQL.`);

    const client = new MongoClient('mongodb://127.0.0.1:27017');
    await client.connect();
    const db = client.db('teamflow_ai');
    const collection = db.collection('employee_profiles');

    for (const emp of employees) {
      const doc = {
        _id: emp.id,
        fullName: `${emp.first_name} ${emp.last_name}`,
        email: emp.work_email,
        departmentId: emp.department_id,
        skills: [],
        weeklyCapacityHours: emp.weekly_capacity_hours || 40,
        active: emp.active === 1,
        _class: 'com.teamflow.ai.ai.document.EmployeeProfile'
      };
      
      await collection.updateOne({ _id: emp.id }, { $set: doc }, { upsert: true });
      console.log(`Upserted ${doc.fullName} in MongoDB`);
    }

    await client.close();
    await mysqlConn.end();
    console.log('Sync complete');
  } catch (err) {
    console.error(err);
  }
}

sync();
