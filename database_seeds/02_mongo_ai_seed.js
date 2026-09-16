// ==============================================================================
// TEAMFLOW AI MONGODB SEED SCRIPT
// Seeds AI recommendation history, audit logs, and in-app notifications.
// Run with: mongosh 02_mongo_ai_seed.js
// ==============================================================================

function seedDatabase(dbName) {
  print(`\n--- Seeding MongoDB Database: ${dbName} ---`);
  const targetDb = connect(`mongodb://localhost:27017/${dbName}`);

  print('Seeding AI Recommendation History...');
  targetDb.recommendation_history.deleteMany({});

  targetDb.recommendation_history.insertMany([
    {
      _class: 'com.teamflow.ai.ai.document.RecommendationHistoryDocument',
      type: 'TASK_ALLOCATION',
      status: 'ACCEPTED',
      inputSummary: {
        taskId: '20000000-0000-4000-8000-000000000001',
        taskTitle: 'AI Workload Prediction Engine',
        requiredSkills: ['Python', 'Machine Learning', 'TensorFlow', 'REST API'],
        estimatedHours: 40,
        priority: 'HIGH'
      },
      recommendations: [
        {
          employeeId: '50b9b5b9-53b4-4019-a5d4-5a6a3fe3405b',
          name: 'Purvesh Patil',
          matchScore: 94.5,
          skillCoverage: 0.95,
          currentWorkloadHours: 24,
          confidence: 0.92,
          reasoning: 'Primary AI engineer with deep ML experience and optimal current weekly bandwidth.'
        },
        {
          employeeId: '8dba6c02-e92a-4c22-9303-1c131916399d',
          name: 'Amod Purvesh Patil',
          matchScore: 88.0,
          skillCoverage: 0.85,
          currentWorkloadHours: 32,
          confidence: 0.87,
          reasoning: 'Strong Python stack developer with full-stack data modeling proficiency.'
        }
      ],
      chosenEmployeeId: '50b9b5b9-53b4-4019-a5d4-5a6a3fe3405b',
      feedbackNotes: 'Optimal assignment based on model specialization.',
      createdAt: new Date()
    },
    {
      _class: 'com.teamflow.ai.ai.document.RecommendationHistoryDocument',
      type: 'TASK_ALLOCATION',
      status: 'ACCEPTED',
      inputSummary: {
        taskId: '20000000-0000-4000-8000-000000000002',
        taskTitle: 'Real-time WebSocket Push Notifications',
        requiredSkills: ['Java', 'Spring Boot', 'STOMP', 'Redis'],
        estimatedHours: 24,
        priority: 'HIGH'
      },
      recommendations: [
        {
          employeeId: '8dba6c02-e92a-4c22-9303-1c131916399d',
          name: 'Amod Purvesh Patil',
          matchScore: 96.0,
          skillCoverage: 1.0,
          currentWorkloadHours: 28,
          confidence: 0.95,
          reasoning: 'Senior backend architect specializing in event messaging and real-time streaming.'
        }
      ],
      chosenEmployeeId: '8dba6c02-e92a-4c22-9303-1c131916399d',
      feedbackNotes: 'Accepted recommendation immediately.',
      createdAt: new Date(Date.now() - 3600000 * 24)
    },
    {
      _class: 'com.teamflow.ai.ai.document.RecommendationHistoryDocument',
      type: 'TASK_ALLOCATION',
      status: 'ACCEPTED',
      inputSummary: {
        taskId: '20000000-0000-4000-8000-000000000003',
        taskTitle: 'API Gateway Security Layer',
        requiredSkills: ['Spring Cloud Gateway', 'JWT', 'OAuth2', 'Security'],
        estimatedHours: 30,
        priority: 'CRITICAL'
      },
      recommendations: [
        {
          employeeId: 'fd53419d-a561-4f4a-90d6-116d5753ced3',
          name: 'Purvesh Developer',
          matchScore: 91.0,
          skillCoverage: 0.90,
          currentWorkloadHours: 20,
          confidence: 0.89,
          reasoning: 'Specialized in microservice gateway security and token lifecycle management.'
        }
      ],
      chosenEmployeeId: 'fd53419d-a561-4f4a-90d6-116d5753ced3',
      feedbackNotes: 'Assigned to security specialist.',
      createdAt: new Date(Date.now() - 3600000 * 48)
    }
  ]);

  print('Seeding In-App Notifications...');
  targetDb.notifications.deleteMany({});

  targetDb.notifications.insertMany([
    // For Purvesh Patil (Super Admin - 50b9b5b9-53b4-4019-a5d4-5a6a3fe3405b)
    {
      _id: `${dbName}-notif-001`,
      _class: 'com.teamflow.ai.ai.document.NotificationDocument',
      recipientEmployeeId: '50b9b5b9-53b4-4019-a5d4-5a6a3fe3405b',
      title: 'AI Smart Task Allocation Complete',
      body: 'AI model has recommended the top candidate for "AI Workload Prediction Engine" with 94.5% skill match score.',
      category: 'AI_INSIGHTS',
      targetUrl: '/ai-insights',
      read: false,
      createdAt: new Date(Date.now() - 1000 * 60 * 15)
    },
    {
      _id: `${dbName}-notif-002`,
      _class: 'com.teamflow.ai.ai.document.NotificationDocument',
      recipientEmployeeId: '50b9b5b9-53b4-4019-a5d4-5a6a3fe3405b',
      title: 'Sprint Velocity on Track',
      body: 'Vantara Builder Websites - Sprint 1 has reached 85% completion milestone ahead of schedule.',
      category: 'PROJECT',
      targetUrl: '/projects',
      read: false,
      createdAt: new Date(Date.now() - 1000 * 60 * 120)
    },
    {
      _id: `${dbName}-notif-003`,
      _class: 'com.teamflow.ai.ai.document.NotificationDocument',
      recipientEmployeeId: '50b9b5b9-53b4-4019-a5d4-5a6a3fe3405b',
      title: 'New Work Log Timesheet Entry',
      body: 'Amod Purvesh Patil logged 5.0 hours on "Real-time WebSocket Push Notifications".',
      category: 'TIMESHEET',
      targetUrl: '/worklogs',
      read: true,
      createdAt: new Date(Date.now() - 1000 * 60 * 360)
    },

    // For Amod Purvesh Patil (Manager - 8dba6c02-e92a-4c22-9303-1c131916399d)
    {
      _id: `${dbName}-notif-004`,
      _class: 'com.teamflow.ai.ai.document.NotificationDocument',
      recipientEmployeeId: '8dba6c02-e92a-4c22-9303-1c131916399d',
      title: 'Pending Leave Approval',
      body: 'Purvesh Developer submitted an Annual Leave request for review.',
      category: 'APPROVAL',
      targetUrl: '/approvals',
      read: false,
      createdAt: new Date(Date.now() - 1000 * 60 * 45)
    },
    {
      _id: `${dbName}-notif-005`,
      _class: 'com.teamflow.ai.ai.document.NotificationDocument',
      recipientEmployeeId: '8dba6c02-e92a-4c22-9303-1c131916399d',
      title: 'High Priority Task Assigned',
      body: 'You have been assigned to lead "Real-time WebSocket Push Notifications".',
      category: 'TASK',
      targetUrl: '/tasks',
      read: false,
      createdAt: new Date(Date.now() - 1000 * 60 * 240)
    },

    // For Purvesh Developer (Employee - fd53419d-a561-4f4a-90d6-116d5753ced3)
    {
      _id: `${dbName}-notif-006`,
      _class: 'com.teamflow.ai.ai.document.NotificationDocument',
      recipientEmployeeId: 'fd53419d-a561-4f4a-90d6-116d5753ced3',
      title: 'New Task Assignment',
      body: 'You have been assigned to "API Gateway Security Layer" (Priority: Critical).',
      category: 'TASK',
      targetUrl: '/tasks',
      read: false,
      createdAt: new Date(Date.now() - 1000 * 60 * 90)
    }
  ]);

  print(`Database ${dbName} seed complete!`);
}

seedDatabase('teamflow_ai');
seedDatabase('test');

print('\nAll MongoDB databases seeded successfully!');
