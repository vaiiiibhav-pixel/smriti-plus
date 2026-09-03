import 'dart:async';
import 'dart:math';

import 'package:flutter/material.dart';
import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:flutter_tts/flutter_tts.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:speech_to_text/speech_to_text.dart';
import 'package:timezone/data/latest.dart' as tz;
import 'package:timezone/timezone.dart' as tz;

class CognitiveAI {
  static Map<String, dynamic> analyze({
    required double score,
    required double accuracy,
    required double reactionTime,
    required int mistakes,
    required List<double> recentScores,
  }) {
    String status;
    String message;
    String recommendation;

    // Analyze recent performance trend
    double trend = 0;

    if (recentScores.length >= 2) {
      trend = recentScores.last - recentScores.first;
    }

    // Overall cognitive performance
    if (score >= 80 && trend >= 0) {
      status = "Excellent";
      message =
          "Your memory performance is strong and showing positive progress.";
      recommendation =
          "You are ready for more challenging memory exercises.";
    } else if (score >= 65) {
      status = "Good";
      message =
          "Your cognitive performance is stable and progressing well.";
      recommendation =
          "Continue practicing at your current difficulty level.";
    } else if (accuracy < 60) {
      status = "Needs Practice";
      message =
          "Your accuracy has decreased recently. Regular practice can help.";
      recommendation =
          "Focus on memory accuracy exercises before increasing difficulty.";
    } else if (reactionTime > 15) {
      status = "Needs Practice";
      message =
          "Your responses are taking slightly longer than expected.";
      recommendation =
          "Try short, regular exercises to improve reaction speed.";
    } else {
      status = "Stable";
      message =
          "Your cognitive performance is stable.";
      recommendation =
          "Continue your daily brain exercises to maintain your progress.";
    }

    return {
      "status": status,
      "message": message,
      "recommendation": recommendation,
      "trend": trend,
    };
  }
}

class ReminderService {
  static final FlutterLocalNotificationsPlugin _plugin =
      FlutterLocalNotificationsPlugin();

  static Future<void> init() async {
    tz.initializeTimeZones();
    tz.setLocalLocation(tz.getLocation('Asia/Kolkata'));

    await _plugin.initialize(
      const InitializationSettings(
        android: AndroidInitializationSettings('@mipmap/ic_launcher'),
      ),
    );

    final android = _plugin.resolvePlatformSpecificImplementation<
        AndroidFlutterLocalNotificationsPlugin>();
    await android?.requestNotificationsPermission();
    await android?.requestExactAlarmsPermission();
  }

  static Future<void> schedule({
    required int id,
    required String title,
    required TimeOfDay time,
  }) async {
    final now = tz.TZDateTime.now(tz.local);
    var when = tz.TZDateTime(
        tz.local, now.year, now.month, now.day, time.hour, time.minute);
    if (when.isBefore(now)) when = when.add(const Duration(days: 1));

    await _plugin.zonedSchedule(
      id,
      'SMRITI+ Reminder',
      title,
      when,
      const NotificationDetails(
        android: AndroidNotificationDetails(
          'smriti_reminders',
          'Reminders',
          channelDescription: 'Medication and daily reminders',
          importance: Importance.max,
          priority: Priority.high,
        ),
      ),
      androidScheduleMode: AndroidScheduleMode.exactAllowWhileIdle,
      uiLocalNotificationDateInterpretation:
          UILocalNotificationDateInterpretation.absoluteTime,
      matchDateTimeComponents: DateTimeComponents.time,
    );
  }

  static Future<void> cancel(int id) => _plugin.cancel(id);
}

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await ReminderService.init();
  runApp(const SmritiApp());
}

// ============================================================
// SMRITI+ APP
// ============================================================

class SmritiApp extends StatelessWidget {
  const SmritiApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      debugShowCheckedModeBanner: false,
      title: 'SMRITI+',
      theme: ThemeData(
        useMaterial3: true,
        textTheme: GoogleFonts.notoSansTextTheme(),
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.indigo),
      ),
      home: const HomePage(),
    );
  }
}

// ============================================================
// HOME PAGE
// ============================================================

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  List<Map<String, String>> reminders = [];
  String userName = '';

  String get greeting {
    final hour = DateTime.now().hour;
    if (hour < 12) return 'Good Morning';
    if (hour < 17) return 'Good Afternoon';
    return 'Good Evening';
  }

  int gamesCompleted = 0;
  int bestScore = 0;
  double averageScore = 0;
  double averageAccuracy = 0;
  double averageReactionTime = 0;
  int currentDifficulty = 3;

  List<Map<String, dynamic>> history = [];

  @override
  void initState() {
    super.initState();
    loadProgress();
    loadReminders();
    loadName();
  }

  // ==========================================================
  // LOAD SAVED DATA
  // ==========================================================

  Future<void> loadProgress() async {
    final prefs = await SharedPreferences.getInstance();

    final scores = prefs.getStringList('scores') ?? [];
    final accuracies = prefs.getStringList('accuracies') ?? [];
    final reactions = prefs.getStringList('reactions') ?? [];
    final difficulties = prefs.getStringList('difficulties') ?? [];

    int totalScore = 0;
    int totalAccuracy = 0;
    double totalReaction = 0;

    List<Map<String, dynamic>> loadedHistory = [];

    for (int i = 0; i < scores.length; i++) {
      final score = int.tryParse(scores[i]) ?? 0;

      final accuracy = i < accuracies.length
          ? double.tryParse(accuracies[i]) ?? 0
          : 0;

      final reaction = i < reactions.length
          ? double.tryParse(reactions[i]) ?? 0
          : 0;

      final difficulty = i < difficulties.length
          ? int.tryParse(difficulties[i]) ?? 3
          : 3;

      totalScore += score;
      totalAccuracy += accuracy.round();
      totalReaction += reaction;

      loadedHistory.add({
        'score': score,
        'accuracy': accuracy,
        'reaction': reaction,
        'difficulty': difficulty,
      });
    }

    if (!mounted) return;

    setState(() {
      gamesCompleted = scores.length;

      if (scores.isNotEmpty) {
        bestScore = scores
            .map((e) => int.tryParse(e) ?? 0)
            .reduce(max);

        averageScore = totalScore / scores.length;
        averageAccuracy = totalAccuracy / scores.length;
        averageReactionTime = totalReaction / scores.length;
      }

      currentDifficulty = prefs.getInt('difficulty') ?? 3;

      history = loadedHistory.reversed.take(5).toList();
    });
  }

  Future<void> loadReminders() async {
    final prefs = await SharedPreferences.getInstance();
    final saved = prefs.getStringList('reminders') ?? [];

    final loaded = saved.map((entry) {
      final parts = entry.split('|');
      return {
        "title": parts.isNotEmpty ? parts[0] : "",
        "time": parts.length > 1 ? parts[1] : "",
      };
    }).toList();

    if (!mounted) return;
    setState(() => reminders = loaded);
  }

  Future<void> saveReminders() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setStringList(
      'reminders',
      reminders.map((r) => '${r["title"]}|${r["time"]}').toList(),
    );
  }

  Future<void> loadName() async {
    final prefs = await SharedPreferences.getInstance();
    final saved = prefs.getString('userName') ?? '';
    if (!mounted) return;
    if (saved.isEmpty) {
      askName();
    } else {
      setState(() => userName = saved);
    }
  }

  Future<void> askName() async {
    final controller = TextEditingController();
    final name = await showDialog<String>(
      context: context,
      barrierDismissible: false,
      builder: (context) => AlertDialog(
        title: const Text('Welcome to SMRITI+'),
        content: TextField(
          controller: controller,
          style: const TextStyle(fontSize: 20),
          decoration: const InputDecoration(labelText: 'Your name'),
        ),
        actions: [
          ElevatedButton(
            onPressed: () => Navigator.pop(context, controller.text.trim()),
            child: const Text('Continue'),
          ),
        ],
      ),
    );

    if (name == null || name.isEmpty) return;
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString('userName', name);
    if (!mounted) return;
    setState(() => userName = name);
  }

  Future<void> addReminder() async {
    final titleController = TextEditingController();
    TimeOfDay? pickedTime;

    final result = await showDialog<bool>(
      context: context,
      builder: (context) => StatefulBuilder(
        builder: (context, setDialogState) => AlertDialog(
          title: const Text('New Reminder'),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              TextField(
                controller: titleController,
                style: const TextStyle(fontSize: 18),
                decoration: const InputDecoration(
                  labelText: 'What to remember',
                  hintText: 'e.g. Take medicine',
                ),
              ),
              const SizedBox(height: 20),
              OutlinedButton.icon(
                icon: const Icon(Icons.access_time),
                label: Text(
                  pickedTime == null
                      ? 'Choose time'
                      : pickedTime!.format(context),
                  style: const TextStyle(fontSize: 17),
                ),
                onPressed: () async {
                  final t = await showTimePicker(
                    context: context,
                    initialTime: TimeOfDay.now(),
                  );
                  if (t != null) setDialogState(() => pickedTime = t);
                },
              ),
            ],
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(context, false),
              child: const Text('Cancel'),
            ),
            ElevatedButton(
              onPressed: () => Navigator.pop(context, true),
              child: const Text('Save'),
            ),
          ],
        ),
      ),
    );

    if (result != true) return;
    if (titleController.text.trim().isEmpty || pickedTime == null) return;
    if (!mounted) return;

    setState(() {
      reminders.add({
        "title": titleController.text.trim(),
        "time": pickedTime!.format(context),
      });
    });

    await saveReminders();
    await ReminderService.schedule(
      id: reminders.length - 1,
      title: titleController.text.trim(),
      time: pickedTime!,
    );
  }

  // ==========================================================
  // OPEN GAME
  // ==========================================================

  Future<void> openGame() async {
    await Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => const MemoryGamePage(),
      ),
    );

    loadProgress();
  }

  // ==========================================================
  // OPEN CAREGIVER DASHBOARD
  // ==========================================================

  Future<void> openCaregiverDashboard() async {
    await Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => const CaregiverDashboard(),
      ),
    );

    loadProgress();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        centerTitle: true,
        title: const Text(
          'SMRITI+',
          style: TextStyle(
            fontSize: 25,
            fontWeight: FontWeight.bold,
          ),
        ),
      ),

      body: RefreshIndicator(
        onRefresh: loadProgress,

        child: SingleChildScrollView(
          physics: const AlwaysScrollableScrollPhysics(),

          padding: const EdgeInsets.all(20),

          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,

            children: [

              // =================================================
              // GREETING
              // =================================================

              const Text(
                'नमस्ते 🙏',
                style: TextStyle(
                  fontSize: 34,
                  fontWeight: FontWeight.bold,
                ),
              ),

              const SizedBox(height: 5),

              Text(
                greeting,
                style: const TextStyle(fontSize: 21),
              ),

              const SizedBox(height: 10),

              Text(
                userName.isEmpty ? 'Hello 👋' : 'Hello, $userName 👋',
                style: const TextStyle(
                  fontSize: 25,
                  fontWeight: FontWeight.w600,
                ),
              ),

              const SizedBox(height: 30),

              // =================================================
              // PROGRESS
              // =================================================

              const Text(
                'Your Progress',
                style: TextStyle(
                  fontSize: 25,
                  fontWeight: FontWeight.bold,
                ),
              ),

              const SizedBox(height: 15),

              buildProgressGrid(),

              const SizedBox(height: 30),

              // =================================================
              // BRAIN EXERCISE
              // =================================================

              Container(
                width: double.infinity,

                padding: const EdgeInsets.all(22),

                decoration: BoxDecoration(
                  color: Theme.of(context).colorScheme.primaryContainer,
                  borderRadius: BorderRadius.circular(22),
                ),

                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,

                  children: [

                    const Text(
                      '🧠 Today\'s Brain Exercise',
                      style: TextStyle(
                        fontSize: 23,
                        fontWeight: FontWeight.bold,
                      ),
                    ),

                    const SizedBox(height: 15),

                    const Text(
                      'Memory Sequence',
                      style: TextStyle(
                        fontSize: 28,
                        fontWeight: FontWeight.bold,
                      ),
                    ),

                    const SizedBox(height: 8),

                    Text(
                      'Current difficulty: $currentDifficulty blocks',
                      style: const TextStyle(
                        fontSize: 18,
                      ),
                    ),

                    const SizedBox(height: 22),

                    SizedBox(
                      width: double.infinity,
                      height: 60,

                      child: ElevatedButton(
                        onPressed: openGame,

                        child: const Text(
                          'START',
                          style: TextStyle(
                            fontSize: 22,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                      ),
                    ),
                  ],
                ),
              ),

              const SizedBox(height: 30),

              // =================================================
              // RECENT PERFORMANCE
              // =================================================

              const Text(
                'Recent Performance',
                style: TextStyle(
                  fontSize: 25,
                  fontWeight: FontWeight.bold,
                ),
              ),

              const SizedBox(height: 15),

              buildHistory(),

              const SizedBox(height: 30),

              // =================================================
              // REMINDERS
              // =================================================

              const Text(
                'Today\'s Reminders',
                style: TextStyle(
                  fontSize: 25,
                  fontWeight: FontWeight.bold,
                ),
              ),

              const SizedBox(height: 15),

              if (reminders.isEmpty)
                Container(
                  width: double.infinity,
                  padding: const EdgeInsets.all(20),
                  decoration: BoxDecoration(
                    color: Colors.grey.shade100,
                    borderRadius: BorderRadius.circular(16),
                  ),
                  child: const Text(
                    'No reminders yet.\nTap "Add Reminder" to create one.',
                    textAlign: TextAlign.center,
                    style: TextStyle(fontSize: 17),
                  ),
                ),

              if (reminders.isNotEmpty)
                Column(
                  children: reminders.map((reminder) {
                    return Card(
                      margin: const EdgeInsets.only(bottom: 10),
                      child: ListTile(
                        leading: const Icon(Icons.notifications_active),
                        title: Text(
                          reminder["title"]!,
                          style: const TextStyle(
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                        subtitle: Text(reminder["time"]!),
                        trailing: IconButton(
                          icon: const Icon(Icons.delete_outline),
                          onPressed: () async {
                            await ReminderService.cancel(reminders.indexOf(reminder));
                            setState(() => reminders.remove(reminder));
                            await saveReminders();
                          },
                        ),
                      ),
                    );
                  }).toList(),
                ),

              const SizedBox(height: 10),

              OutlinedButton.icon(
                onPressed: addReminder,
                icon: const Icon(Icons.add_alarm),
                label: const Text("Add Reminder"),
              ),

              const SizedBox(height: 25),

              // =================================================
              // CAREGIVER DASHBOARD BUTTON
              // =================================================

              Container(
                width: double.infinity,

                padding: const EdgeInsets.all(20),

                decoration: BoxDecoration(
                  color: Colors.indigo.shade50,
                  borderRadius: BorderRadius.circular(20),
                  border: Border.all(
                    color: Colors.indigo.shade100,
                  ),
                ),

                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,

                  children: [

                    const Text(
                      '👨‍⚕️ Caregiver Access',
                      style: TextStyle(
                        fontSize: 22,
                        fontWeight: FontWeight.bold,
                      ),
                    ),

                    const SizedBox(height: 8),

                    Text(
                      'Monitor cognitive performance, '
                      'activity and reminders.',
                      style: TextStyle(
                        fontSize: 16,
                        color: Colors.grey.shade700,
                      ),
                    ),

                    const SizedBox(height: 16),

                    SizedBox(
                      width: double.infinity,
                      height: 55,

                      child: ElevatedButton.icon(
                        onPressed: openCaregiverDashboard,

                        icon: const Icon(
                          Icons.dashboard_rounded,
                        ),

                        label: const Text(
                          'OPEN CAREGIVER DASHBOARD',
                          style: TextStyle(
                            fontSize: 17,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                      ),
                    ),
                  ],
                ),
              ),

              const SizedBox(height: 25),

              // =================================================
              // VOICE ASSISTANT
              // =================================================

              SizedBox(
                width: double.infinity,
                height: 60,

                child: ElevatedButton.icon(
                  onPressed: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (context) => const VoiceAssistantPage(),
                      ),
                    );
                  },

                  icon: const Text(
                    '🎤',
                    style: TextStyle(
                      fontSize: 26,
                    ),
                  ),

                  label: const Text(
                    'Talk to me',
                    style: TextStyle(
                      fontSize: 22,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ),
              ),

              const SizedBox(height: 20),
            ],
          ),
        ),
      ),
    );
  }

  // ==========================================================
  // PROGRESS GRID
  // ==========================================================

  Widget buildProgressGrid() {
    return GridView.count(
      crossAxisCount: 2,

      shrinkWrap: true,

      physics: const NeverScrollableScrollPhysics(),

      crossAxisSpacing: 12,
      mainAxisSpacing: 12,

      childAspectRatio: 1.7,

      children: [

        progressCard(
          icon: Icons.sports_esports_rounded,
          title: 'Games',
          value: '$gamesCompleted',
        ),

        progressCard(
          icon: Icons.emoji_events_rounded,
          title: 'Best Score',
          value: '$bestScore',
        ),

        progressCard(
          icon: Icons.psychology_rounded,
          title: 'Average Score',
          value: averageScore.toStringAsFixed(0),
        ),

        progressCard(
          icon: Icons.gps_fixed_rounded,
          title: 'Accuracy',
          value: '${averageAccuracy.toStringAsFixed(0)}%',
        ),
      ],
    );
  }

  // ==========================================================
  // PROGRESS CARD
  // ==========================================================

  Widget progressCard({
    required IconData icon,
    required String title,
    required String value,
  }) {
    return Card(
      elevation: 1,

      child: Padding(
        padding: const EdgeInsets.all(14),

        child: Row(
          children: [

            Icon(
              icon,
              size: 28,
              color: Colors.indigo,
            ),

            const SizedBox(width: 10),

            Expanded(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                crossAxisAlignment: CrossAxisAlignment.start,

                children: [

                  Text(
                    title,
                    style: const TextStyle(
                      fontSize: 15,
                    ),
                  ),

                  const SizedBox(height: 3),

                  Text(
                    value,
                    style: const TextStyle(
                      fontSize: 22,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  // ==========================================================
  // HISTORY
  // ==========================================================

  Widget buildHistory() {
    if (history.isEmpty) {
      return Container(
        width: double.infinity,

        padding: const EdgeInsets.all(20),

        decoration: BoxDecoration(
          color: Colors.grey.shade100,
          borderRadius: BorderRadius.circular(16),
        ),

        child: const Text(
          'No games completed yet.\nStart your first memory exercise!',
          textAlign: TextAlign.center,

          style: TextStyle(
            fontSize: 18,
          ),
        ),
      );
    }

    return Column(
      children: history.asMap().entries.map((entry) {
        final game = entry.value;

        return Card(
          margin: const EdgeInsets.only(bottom: 10),

          child: ListTile(
            leading: CircleAvatar(
              child: Text(
                '${entry.key + 1}',
                style: const TextStyle(
                  fontWeight: FontWeight.bold,
                ),
              ),
            ),

            title: Text(
              'Score: ${game['score']}',
              style: const TextStyle(
                fontSize: 18,
                fontWeight: FontWeight.bold,
              ),
            ),

            subtitle: Text(
              'Accuracy: ${game['accuracy'].toStringAsFixed(0)}%  •  '
              'Time: ${game['reaction'].toStringAsFixed(1)}s',
            ),

            trailing: Text(
              '${game['difficulty']} blocks',
              style: const TextStyle(
                fontWeight: FontWeight.bold,
              ),
            ),
          ),
        );
      }).toList(),
    );
  }

  // ==========================================================
  // REMINDER CARD
  // ==========================================================

  Widget reminderCard({
    required String icon,
    required String title,
    required String time,
  }) {
    return Card(
      margin: const EdgeInsets.only(bottom: 12),

      child: ListTile(
        contentPadding: const EdgeInsets.symmetric(
          horizontal: 18,
          vertical: 10,
        ),

        leading: Text(
          icon,
          style: const TextStyle(
            fontSize: 32,
          ),
        ),

        title: Text(
          title,
          style: const TextStyle(
            fontSize: 20,
            fontWeight: FontWeight.w600,
          ),
        ),

        trailing: Text(
          time,
          style: const TextStyle(
            fontSize: 17,
            fontWeight: FontWeight.bold,
          ),
        ),
      ),
    );
  }
}

// ============================================================
// CAREGIVER DASHBOARD
// ============================================================

class CaregiverDashboard extends StatefulWidget {
  const CaregiverDashboard({super.key});

  @override
  State<CaregiverDashboard> createState() => _CaregiverDashboardState();
}

class _CaregiverDashboardState extends State<CaregiverDashboard> {

  bool loading = true;

  String userName = 'the user';

  int gamesCompleted = 0;
  int bestScore = 0;

  double averageScore = 0;
  double averageAccuracy = 0;
  double averageReactionTime = 0;

  int currentDifficulty = 3;

  List<Map<String, dynamic>> history = [];
  List<Map<String, String>> reminders = [];

  @override
  void initState() {
    super.initState();

    loadDashboard();
  }

  // ==========================================================
  // LOAD DASHBOARD DATA
  // ==========================================================

  Future<void> loadDashboard() async {

    final prefs = await SharedPreferences.getInstance();

    final scores =
        prefs.getStringList('scores') ?? [];

    final accuracies =
        prefs.getStringList('accuracies') ?? [];

    final reactions =
        prefs.getStringList('reactions') ?? [];

    final difficulties =
        prefs.getStringList('difficulties') ?? [];

    final savedReminders = prefs.getStringList('reminders') ?? [];
    final loadedReminders = savedReminders.map((entry) {
      final parts = entry.split('|');
      return {
        "title": parts.isNotEmpty ? parts[0] : "",
        "time": parts.length > 1 ? parts[1] : "",
      };
    }).toList();

    int totalScore = 0;

    double totalAccuracy = 0;

    double totalReaction = 0;

    final loadedHistory =
        <Map<String, dynamic>>[];

    for (int i = 0; i < scores.length; i++) {

      final score =
          int.tryParse(scores[i]) ?? 0;

      final accuracy =
          i < accuracies.length
              ? double.tryParse(accuracies[i]) ?? 0
              : 0;

      final reaction =
          i < reactions.length
              ? double.tryParse(reactions[i]) ?? 0
              : 0;

      final difficulty =
          i < difficulties.length
              ? int.tryParse(difficulties[i]) ?? 3
              : 3;

      totalScore += score;

      totalAccuracy += accuracy;

      totalReaction += reaction;

      loadedHistory.add({
        'score': score,
        'accuracy': accuracy,
        'reaction': reaction,
        'difficulty': difficulty,
      });
    }

    if (!mounted) return;

    setState(() {

      gamesCompleted = scores.length;

      if (scores.isNotEmpty) {

        bestScore = scores
            .map((e) => int.tryParse(e) ?? 0)
            .reduce(max);

        averageScore =
            totalScore / scores.length;

        averageAccuracy =
            totalAccuracy / scores.length;

        averageReactionTime =
            totalReaction / scores.length;
      }

      currentDifficulty =
          prefs.getInt('difficulty') ?? 3;

      userName = prefs.getString('userName') ?? 'the user';

      history =
          loadedHistory.reversed.take(8).toList();

      reminders = loadedReminders;

      loading = false;
    });
  }

  // ==========================================================
  // WELLNESS STATUS
  // ==========================================================

  String get wellnessStatus {

    if (gamesCompleted == 0) {
      return 'No Data Yet';
    }

    if (averageScore >= 70) {
      return 'Good';
    }

    if (averageScore >= 50) {
      return 'Needs Monitoring';
    }

    return 'Needs Attention';
  }

  // ==========================================================
  // WELLNESS ICON
  // ==========================================================

  String get wellnessIcon {

    if (gamesCompleted == 0) {
      return 'ℹ️';
    }

    if (averageScore >= 70) {
      return '🟢';
    }

    if (averageScore >= 50) {
      return '🟡';
    }

    return '🔴';
  }

  // ==========================================================
  // WELLNESS MESSAGE
  // ==========================================================

  String get wellnessMessage {

    if (gamesCompleted == 0) {
      return 'Complete a few memory exercises to generate '
          'a cognitive performance overview.';
    }

    if (averageScore >= 70) {
      return '$userName is showing good cognitive exercise '
          'performance based on recent sessions.';
    }

    if (averageScore >= 50) {
      return 'Performance is moderate. Continue regular '
          'cognitive exercises and monitor progress.';
    }

    return 'Recent performance is lower than expected. '
        'Consider monitoring upcoming sessions closely.';
  }

  String getPerformanceTrend(List<double> scores) {
    if (scores.length < 2) {
      return "Not enough data";
    }

    final chronological = scores.reversed.toList();

    final recent = chronological.length >= 3
        ? chronological.sublist(chronological.length - 3)
        : chronological;

    final difference = recent.last - recent.first;

    if (difference >= 10) {
      return "Improving";
    }

    if (difference <= -10) {
      return "Needs Attention";
    }

    return "Stable";
  }

  // ==========================================================
  // AI CAREGIVER INSIGHT
  // ==========================================================

  String generateCaregiverInsight({
    required double averageScore,
    required double averageAccuracy,
    required double averageReaction,
    required int games,
  }) {
    if (games == 0) {
      return "No cognitive activity recorded yet. Encourage the user to complete their first memory exercise.";
    }

    if (averageScore >= 80 && averageAccuracy >= 80) {
      return "Recent cognitive performance is strong. The user is performing well in memory exercises and maintaining good accuracy. Consider gradually increasing exercise difficulty.";
    }

    if (averageAccuracy < 60) {
      return "Memory accuracy appears lower than the recent target range. Consider continuing with easier exercises and encouraging short, regular practice sessions.";
    }

    if (averageReaction > 15) {
      return "Response speed is relatively slow. Short, regular cognitive exercises may help maintain engagement and improve response speed.";
    }

    if (averageScore >= 65) {
      return "Cognitive performance is stable and progressing well. Continue regular memory exercises at the current difficulty.";
    }

    return "The user's cognitive performance is being monitored. Continue regular exercises and observe changes over time.";
  }

  // ==========================================================
  // BUILD
  // ==========================================================

  @override
  Widget build(BuildContext context) {

    return Scaffold(

      appBar: AppBar(

        centerTitle: false,

        title: const Column(
          crossAxisAlignment: CrossAxisAlignment.start,

          children: [

            Text(
              'SMRITI+',
              style: TextStyle(
                fontSize: 21,
                fontWeight: FontWeight.bold,
              ),
            ),

            Text(
              'Caregiver Dashboard',
              style: TextStyle(
                fontSize: 14,
              ),
            ),
          ],
        ),

        actions: [

          IconButton(
            onPressed: loadDashboard,

            tooltip: 'Refresh',

            icon: const Icon(
              Icons.refresh_rounded,
            ),
          ),
        ],
      ),

      body: loading

          ? const Center(
              child: CircularProgressIndicator(),
            )

          : RefreshIndicator(

              onRefresh: loadDashboard,

              child: SingleChildScrollView(

                physics:
                    const AlwaysScrollableScrollPhysics(),

                padding:
                    const EdgeInsets.all(20),

                child: Column(

                  crossAxisAlignment:
                      CrossAxisAlignment.start,

                  children: [

                    // ==================================================
                    // HEADER
                    // ==================================================

                    const Text(
                      'Caregiver Overview',
                      style: TextStyle(
                        fontSize: 28,
                        fontWeight: FontWeight.bold,
                      ),
                    ),

                    const SizedBox(height: 6),

                    Text(
                      'Monitor $userName\'s cognitive activity '
                      'and daily wellness.',
                      style: TextStyle(
                        fontSize: 16,
                        color: Colors.grey.shade700,
                      ),
                    ),

                    const SizedBox(height: 22),

                    // ==================================================
                    // ELDER PROFILE
                    // ==================================================

                    buildProfileCard(),

                    const SizedBox(height: 18),

                    // ==================================================
                    // WELLNESS STATUS
                    // ==================================================

                    buildWellnessCard(),

                    const SizedBox(height: 22),

                    // ==================================================
                    // STATISTICS
                    // ==================================================

                    const Text(
                      'Cognitive Performance',
                      style: TextStyle(
                        fontSize: 23,
                        fontWeight: FontWeight.bold,
                      ),
                    ),

                    const SizedBox(height: 14),

                    buildStatisticsGrid(),

                    const SizedBox(height: 20),

                    Card(
                      elevation: 2,
                      child: ListTile(
                        leading: const Icon(Icons.trending_up),
                        title: const Text(
                          "AI Performance Trend",
                          style: TextStyle(
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                        subtitle: Text(
                          getPerformanceTrend(history
                              .map((entry) => (entry['score'] as num).toDouble())
                              .toList()),
                          style: const TextStyle(
                            fontSize: 16,
                          ),
                        ),
                      ),
                    ),

                    const SizedBox(height: 20),

                    Card(
                      elevation: 3,
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(18),
                      ),
                      child: Padding(
                        padding: const EdgeInsets.all(20),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Row(
                              children: [
                                Container(
                                  padding: const EdgeInsets.all(10),
                                  decoration: BoxDecoration(
                                    color: Theme.of(context)
                                        .colorScheme
                                        .primaryContainer,
                                    borderRadius: BorderRadius.circular(12),
                                  ),
                                  child: Icon(
                                    Icons.psychology,
                                    color: Theme.of(context)
                                        .colorScheme
                                        .onPrimaryContainer,
                                    size: 28,
                                  ),
                                ),
                                const SizedBox(width: 12),
                                const Expanded(
                                  child: Text(
                                    'AI Cognitive Insight',
                                    style: TextStyle(
                                      fontSize: 19,
                                      fontWeight: FontWeight.bold,
                                    ),
                                  ),
                                ),
                              ],
                            ),
                            const SizedBox(height: 16),
                            Text(
                              generateCaregiverInsight(
                                averageScore: averageScore,
                                averageAccuracy: averageAccuracy,
                                averageReaction: averageReactionTime,
                                games: gamesCompleted,
                              ),
                              style: const TextStyle(
                                fontSize: 15,
                                height: 1.5,
                              ),
                            ),
                          ],
                        ),
                      ),
                    ),

                    const SizedBox(height: 25),

                    // ==================================================
                    // PERFORMANCE TREND
                    // ==================================================

                    const Text(
                      'Performance Trend',
                      style: TextStyle(
                        fontSize: 23,
                        fontWeight: FontWeight.bold,
                      ),
                    ),

                    const SizedBox(height: 14),

                    buildPerformanceChart(),

                    const SizedBox(height: 25),

                    // ==================================================
                    // RECENT ACTIVITY
                    // ==================================================

                    const Text(
                      'Recent Activity',
                      style: TextStyle(
                        fontSize: 23,
                        fontWeight: FontWeight.bold,
                      ),
                    ),

                    const SizedBox(height: 14),

                    buildRecentActivity(),

                    const SizedBox(height: 25),

                    // ==================================================
                    // REMINDERS
                    // ==================================================

                    const Text(
                      'Medication & Reminders',
                      style: TextStyle(
                        fontSize: 23,
                        fontWeight: FontWeight.bold,
                      ),
                    ),

                    const SizedBox(height: 14),

                    if (reminders.isEmpty)
                      buildActivityTile(
                        icon: Icons.info_outline_rounded,
                        title: 'No reminders set',
                        subtitle: 'Reminders added by Ram Ji will appear here.',
                      )
                    else
                      ...reminders.map(
                        (r) => buildDashboardReminder(
                          icon: '🔔',
                          title: r["title"] ?? '',
                          subtitle: 'Daily reminder',
                          time: r["time"] ?? '',
                        ),
                      ),

                    const SizedBox(height: 25),

                    // ==================================================
                    // MONITORING NOTE
                    // ==================================================

                    buildMonitoringNote(),

                    const SizedBox(height: 30),
                  ],
                ),
              ),
            ),
    );
  }

  // ==========================================================
  // PROFILE CARD
  // ==========================================================

  Widget buildProfileCard() {

    return Card(

      elevation: 2,

      child: Padding(

        padding: const EdgeInsets.all(20),

        child: Row(

          children: [

            CircleAvatar(

              radius: 34,

              backgroundColor:
                  Colors.indigo.shade100,

              child: const Text(
                '👴',
                style: TextStyle(
                  fontSize: 38,
                ),
              ),
            ),

            const SizedBox(width: 16),

            Expanded(

              child: Column(

                crossAxisAlignment:
                    CrossAxisAlignment.start,

                children: [

                  Text(
                    userName,
                    style: const TextStyle(
                      fontSize: 23,
                      fontWeight: FontWeight.bold,
                    ),
                  ),

                  const SizedBox(height: 4),

                  Text(
                    'Elderly Care Profile',
                    style: TextStyle(
                      fontSize: 15,
                      color: Colors.grey.shade700,
                    ),
                  ),

                  const SizedBox(height: 8),

                  Row(

                    children: [

                      Icon(
                        Icons.circle,
                        size: 10,
                        color: Colors.green.shade600,
                      ),

                      const SizedBox(width: 6),

                      const Text(
                        'Active today',
                        style: TextStyle(
                          fontWeight: FontWeight.w600,
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ),

            const Icon(
              Icons.verified_user_rounded,
              color: Colors.indigo,
              size: 30,
            ),
          ],
        ),
      ),
    );
  }

  // ==========================================================
  // WELLNESS CARD
  // ==========================================================

  Widget buildWellnessCard() {

    return Container(

      width: double.infinity,

      padding: const EdgeInsets.all(20),

      decoration: BoxDecoration(
        color: averageScore >= 70
            ? Colors.green.shade50
            : averageScore >= 50
                ? Colors.orange.shade50
                : Colors.red.shade50,

        borderRadius:
            BorderRadius.circular(20),

        border: Border.all(
          color: averageScore >= 70
              ? Colors.green.shade200
              : averageScore >= 50
                  ? Colors.orange.shade200
                  : Colors.red.shade200,
        ),
      ),

      child: Row(

        crossAxisAlignment:
            CrossAxisAlignment.start,

        children: [

          Text(
            wellnessIcon,
            style: const TextStyle(
              fontSize: 35,
            ),
          ),

          const SizedBox(width: 14),

          Expanded(

            child: Column(

              crossAxisAlignment:
                  CrossAxisAlignment.start,

              children: [

                const Text(
                  'Cognitive Wellness',
                  style: TextStyle(
                    fontSize: 18,
                    fontWeight: FontWeight.bold,
                  ),
                ),

                const SizedBox(height: 4),

                Text(
                  wellnessStatus,
                  style: const TextStyle(
                    fontSize: 25,
                    fontWeight: FontWeight.bold,
                  ),
                ),

                const SizedBox(height: 7),

                Text(
                  wellnessMessage,
                  style: const TextStyle(
                    fontSize: 15,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  // ==========================================================
  // STATISTICS GRID
  // ==========================================================

  Widget buildStatisticsGrid() {

    return GridView.count(

      crossAxisCount: 2,

      shrinkWrap: true,

      physics:
          const NeverScrollableScrollPhysics(),

      crossAxisSpacing: 12,

      mainAxisSpacing: 12,

      childAspectRatio: 1.45,

      children: [

        dashboardStatCard(
          icon: Icons.psychology_rounded,
          title: 'Average Score',
          value:
              averageScore.toStringAsFixed(0),
          suffix: '/100',
        ),

        dashboardStatCard(
          icon: Icons.gps_fixed_rounded,
          title: 'Accuracy',
          value:
              averageAccuracy.toStringAsFixed(0),
          suffix: '%',
        ),

        dashboardStatCard(
          icon: Icons.sports_esports_rounded,
          title: 'Games Completed',
          value:
              '$gamesCompleted',
          suffix: '',
        ),

        dashboardStatCard(
          icon: Icons.speed_rounded,
          title: 'Reaction Time',
          value:
              averageReactionTime.toStringAsFixed(1),
          suffix: ' sec',
        ),

        dashboardStatCard(
          icon: Icons.emoji_events_rounded,
          title: 'Best Score',
          value:
              '$bestScore',
          suffix: '/100',
        ),

        dashboardStatCard(
          icon: Icons.auto_graph_rounded,
          title: 'Difficulty',
          value:
              '$currentDifficulty',
          suffix: ' blocks',
        ),
      ],
    );
  }

  // ==========================================================
  // STAT CARD
  // ==========================================================

  Widget dashboardStatCard({
    required IconData icon,
    required String title,
    required String value,
    required String suffix,
  }) {

    return Card(

      elevation: 1,

      child: Padding(

        padding: const EdgeInsets.all(15),

        child: Column(

          crossAxisAlignment:
              CrossAxisAlignment.start,

          children: [

            Icon(
              icon,
              size: 28,
              color: Colors.indigo,
            ),

            const Spacer(),

            Text(
              title,
              style: TextStyle(
                fontSize: 14,
                color: Colors.grey.shade700,
              ),
            ),

            const SizedBox(height: 3),

            Row(

              crossAxisAlignment:
                  CrossAxisAlignment.end,

              children: [

                Text(
                  value,
                  style: const TextStyle(
                    fontSize: 25,
                    fontWeight: FontWeight.bold,
                  ),
                ),

                if (suffix.isNotEmpty)
                  Padding(
                    padding:
                        const EdgeInsets.only(
                      left: 3,
                      bottom: 3,
                    ),
                    child: Text(
                      suffix,
                      style: TextStyle(
                        fontSize: 13,
                        color:
                            Colors.grey.shade700,
                      ),
                    ),
                  ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  // ==========================================================
  // PERFORMANCE CHART
  // ==========================================================

  Widget buildPerformanceChart() {

    if (history.isEmpty) {

      return Container(

        width: double.infinity,

        height: 220,

        decoration: BoxDecoration(
          color: Colors.grey.shade100,
          borderRadius:
              BorderRadius.circular(20),
        ),

        child: const Center(

          child: Column(

            mainAxisAlignment:
                MainAxisAlignment.center,

            children: [

              Icon(
                Icons.bar_chart_rounded,
                size: 50,
              ),

              SizedBox(height: 10),

              Text(
                'Performance data will appear here',
                style: TextStyle(
                  fontSize: 16,
                ),
              ),
            ],
          ),
        ),
      );
    }

    final chartData =
        history.reversed.toList();

    return Container(

      width: double.infinity,

      padding: const EdgeInsets.all(20),

      decoration: BoxDecoration(
        color: Colors.indigo.shade50,
        borderRadius:
            BorderRadius.circular(20),
        border: Border.all(
          color: Colors.indigo.shade100,
        ),
      ),

      child: Column(

        children: [

          SizedBox(

            height: 190,

            child: Row(

              crossAxisAlignment:
                  CrossAxisAlignment.end,

              children: chartData
                  .asMap()
                  .entries
                  .map((entry) {

                final game =
                    entry.value;

                final score =
                    (game['score'] as int)
                        .toDouble();

                final double height =
                    max(8.0, score * 1.35).toDouble();

                return Expanded(

                  child: Padding(

                    padding:
                        const EdgeInsets
                            .symmetric(
                      horizontal: 5,
                    ),

                    child: Column(

                      mainAxisAlignment:
                          MainAxisAlignment.end,

                      children: [

                        Text(
                          '${score.round()}',
                          style:
                              const TextStyle(
                            fontSize: 12,
                            fontWeight:
                                FontWeight.bold,
                          ),
                        ),

                        const SizedBox(
                          height: 5,
                        ),

                        Container(

                          height: height,

                          decoration:
                              BoxDecoration(
                            color:
                                Colors.indigo,
                            borderRadius:
                                BorderRadius
                                    .circular(
                              8,
                            ),
                          ),
                        ),

                        const SizedBox(
                          height: 8,
                        ),

                        Text(
                          'G${entry.key + 1}',
                          style:
                              TextStyle(
                            fontSize: 11,
                            color: Colors
                                .grey.shade700,
                          ),
                        ),
                      ],
                    ),
                  ),
                );
              }).toList(),
            ),
          ),

          const SizedBox(height: 12),

          Text(
            'Recent memory exercise scores',
            style: TextStyle(
              fontSize: 14,
              color: Colors.grey.shade700,
            ),
          ),
        ],
      ),
    );
  }

  // ==========================================================
  // RECENT ACTIVITY
  // ==========================================================

  Widget buildRecentActivity() {

    if (history.isEmpty) {

      return buildActivityTile(
        icon: Icons.info_outline_rounded,
        title: 'No activity yet',
        subtitle:
            'Start a memory exercise to begin tracking.',
      );
    }

    return Column(

      children: history
          .take(4)
          .toList()
          .asMap()
          .entries
          .map((entry) {

        final game =
            entry.value;

        final score =
            game['score'] as int;

        final accuracy =
            game['accuracy'] as double;

        return buildActivityTile(

          icon:
              score >= 80
                  ? Icons.check_circle_rounded
                  : score >= 50
                      ? Icons.trending_up_rounded
                      : Icons.warning_rounded,

          title:
              'Memory exercise completed',

          subtitle:
              'Score $score • '
              'Accuracy ${accuracy.toStringAsFixed(0)}% • '
              '${game['difficulty']} blocks',
        );

      }).toList(),
    );
  }

  // ==========================================================
  // ACTIVITY TILE
  // ==========================================================

  Widget buildActivityTile({
    required IconData icon,
    required String title,
    required String subtitle,
  }) {

    return Card(

      margin:
          const EdgeInsets.only(bottom: 10),

      child: ListTile(

        contentPadding:
            const EdgeInsets.symmetric(
          horizontal: 16,
          vertical: 6,
        ),

        leading: CircleAvatar(

          backgroundColor:
              Colors.indigo.shade50,

          child: Icon(
            icon,
            color: Colors.indigo,
          ),
        ),

        title: Text(
          title,
          style: const TextStyle(
            fontWeight: FontWeight.bold,
          ),
        ),

        subtitle: Text(
          subtitle,
        ),
      ),
    );
  }

  // ==========================================================
  // DASHBOARD REMINDER
  // ==========================================================

  Widget buildDashboardReminder({
    required String icon,
    required String title,
    required String subtitle,
    required String time,
  }) {

    return Card(

      margin:
          const EdgeInsets.only(bottom: 10),

      child: ListTile(

        contentPadding:
            const EdgeInsets.symmetric(
          horizontal: 16,
          vertical: 7,
        ),

        leading: Text(
          icon,
          style: const TextStyle(
            fontSize: 30,
          ),
        ),

        title: Text(
          title,
          style: const TextStyle(
            fontSize: 17,
            fontWeight: FontWeight.bold,
          ),
        ),

        subtitle: Text(
          subtitle,
        ),

        trailing: Container(

          padding:
              const EdgeInsets.symmetric(
            horizontal: 10,
            vertical: 7,
          ),

          decoration: BoxDecoration(
            color: Colors.indigo.shade50,
            borderRadius:
                BorderRadius.circular(10),
          ),

          child: Text(
            time,
            style: const TextStyle(
              fontWeight: FontWeight.bold,
            ),
          ),
        ),
      ),
    );
  }

  // ==========================================================
  // MONITORING NOTE
  // ==========================================================

  Widget buildMonitoringNote() {

    return Container(

      width: double.infinity,

      padding: const EdgeInsets.all(18),

      decoration: BoxDecoration(
        color: Colors.amber.shade50,
        borderRadius:
            BorderRadius.circular(18),
        border: Border.all(
          color: Colors.amber.shade200,
        ),
      ),

      child: Row(

        crossAxisAlignment:
            CrossAxisAlignment.start,

        children: [

          const Text(
            '⚠️',
            style: TextStyle(
              fontSize: 28,
            ),
          ),

          const SizedBox(width: 12),

          Expanded(

            child: Column(

              crossAxisAlignment:
                  CrossAxisAlignment.start,

              children: [

                const Text(
                  'Caregiver Note',
                  style: TextStyle(
                    fontSize: 17,
                    fontWeight: FontWeight.bold,
                  ),
                ),

                const SizedBox(height: 5),

                Text(
                  gamesCompleted == 0
                      ? 'More activity data is needed before '
                          'SMRITI+ can identify performance trends.'
                      : 'SMRITI+ is currently using recent '
                          'memory-game performance to personalize '
                          'future cognitive exercises.',
                  style: const TextStyle(
                    fontSize: 15,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

// ============================================================
// MEMORY GAME PAGE
// ============================================================

class MemoryGamePage extends StatefulWidget {
  const MemoryGamePage({super.key});

  @override
  State<MemoryGamePage> createState() => _MemoryGamePageState();
}

class _MemoryGamePageState extends State<MemoryGamePage> {

  // ==========================================================
  // COLORS
  // ==========================================================

  final List<Color> availableColors = [
    Colors.red,
    Colors.blue,
    Colors.green,
    Colors.orange,
    Colors.purple,
    Colors.teal,
    Colors.pink,
    Colors.brown,
  ];

  final Random random = Random();

  // ==========================================================
  // GAME DATA
  // ==========================================================

  List<Color> sequence = [];

  List<Color> userSequence = [];

  int difficulty = 3;

  int mistakes = 0;

  int currentRound = 1;

  int correctAnswers = 0;

  int score = 0;

  double reactionTime = 0;

  double accuracy = 0;

  Map<String, dynamic> aiAnalysis = {};

  String gameState = 'loading';

  Timer? countdownTimer;

  int countdown = 3;

  Stopwatch stopwatch = Stopwatch();

  // ==========================================================
  // INIT
  // ==========================================================

  @override
  void initState() {
    super.initState();

    loadDifficulty();
  }

  // ==========================================================
  // LOAD DIFFICULTY
  // ==========================================================

  Future<void> loadDifficulty() async {
    final prefs = await SharedPreferences.getInstance();

    difficulty = prefs.getInt('difficulty') ?? 3;

    if (!mounted) return;

    startRound();
  }

  // ==========================================================
  // GENERATE SEQUENCE
  // ==========================================================

  void generateSequence() {

    sequence = [];

    for (int i = 0; i < difficulty; i++) {

      sequence.add(
        availableColors[
          random.nextInt(
            availableColors.length,
          )
        ],
      );
    }
  }

  // ==========================================================
  // START ROUND
  // ==========================================================

  void startRound() {

    countdownTimer?.cancel();

    setState(() {

      gameState = 'countdown';

      countdown = 3;

      userSequence = [];

      mistakes = 0;

    });

    countdownTimer = Timer.periodic(
      const Duration(seconds: 1),
      (timer) {

        if (countdown > 1) {

          setState(() {
            countdown--;
          });

        } else {

          timer.cancel();

          showSequence();
        }
      },
    );
  }

  // ==========================================================
  // SHOW SEQUENCE
  // ==========================================================

  void showSequence() {

    generateSequence();

    setState(() {
      gameState = 'show';
    });

    Future.delayed(
      const Duration(seconds: 3),
      () {

        if (!mounted) return;

        hideSequence();
      },
    );
  }

  // ==========================================================
  // HIDE SEQUENCE
  // ==========================================================

  void hideSequence() {

    setState(() {

      gameState = 'recall';

      userSequence = [];

    });

    stopwatch
      ..reset()
      ..start();
  }

  // ==========================================================
  // SELECT COLOR
  // ==========================================================

  void selectColor(Color color) async {

    if (gameState != 'recall') {
      return;
    }

    final index = userSequence.length;

    if (index >= sequence.length) {
      return;
    }

    userSequence.add(color);

    if (userSequence[index] != sequence[index]) {
      mistakes++;
    }

    if (userSequence.length ==
        sequence.length) {

      stopwatch.stop();

      reactionTime =
          stopwatch.elapsedMilliseconds / 1000;

      await calculateScore();
    }

    setState(() {});
  }

  // ==========================================================
  // CALCULATE SCORE
  // ==========================================================

  Future<void> calculateScore() async {

    int correct = 0;

    for (int i = 0;
        i < sequence.length;
        i++) {

      if (userSequence[i] ==
          sequence[i]) {

        correct++;
      }
    }

    correctAnswers = correct;

    accuracy =
        (correct / sequence.length) * 100;

    double speedScore;

    if (reactionTime <= 5) {

      speedScore = 100;

    } else if (reactionTime <= 10) {

      speedScore = 80;

    } else if (reactionTime <= 15) {

      speedScore = 60;

    } else if (reactionTime <= 20) {

      speedScore = 40;

    } else {

      speedScore = 20;
    }

    double consistencyScore;

    if (mistakes == 0) {

      consistencyScore = 100;

    } else if (mistakes == 1) {

      consistencyScore = 80;

    } else if (mistakes == 2) {

      consistencyScore = 60;

    } else {

      consistencyScore = 40;
    }

    final overallScore =
        (accuracy * 0.5) +
        (speedScore * 0.3) +
        (consistencyScore * 0.2);

    score = overallScore.round();

    final prefs = await SharedPreferences.getInstance();
    final stored = prefs.getStringList('scores') ?? [];

    final allScores = stored
        .map((e) => double.tryParse(e) ?? 0)
        .toList()
      ..add(overallScore);

    final scores = allScores.length > 5
        ? allScores.sublist(allScores.length - 5)
        : allScores;

    aiAnalysis = CognitiveAI.analyze(
      score: overallScore,
      accuracy: accuracy,
      reactionTime: reactionTime,
      mistakes: mistakes,
      recentScores: scores,
    );

    final playedDifficulty = difficulty;

    // ========================================================
    // ADAPTIVE DIFFICULTY
    // ========================================================

    if (score >= 80) {

      if (difficulty < 8) {
        difficulty++;
      }

    } else if (score < 50) {

      if (difficulty > 3) {
        difficulty--;
      }
    }

    saveResult(playedDifficulty);

    setState(() {
      gameState = 'result';
    });
  }

  // ==========================================================
  // SAVE RESULT
  // ==========================================================

  Future<void> saveResult(int playedDifficulty) async {

    final prefs =
        await SharedPreferences.getInstance();

    List<String> scores =
        prefs.getStringList('scores') ?? [];

    List<String> accuracies =
        prefs.getStringList('accuracies') ?? [];

    List<String> reactions =
        prefs.getStringList('reactions') ?? [];

    List<String> difficulties =
        prefs.getStringList('difficulties') ?? [];

    scores.add(
      score.toString(),
    );

    accuracies.add(
      accuracy.toString(),
    );

    reactions.add(
      reactionTime.toString(),
    );

    difficulties.add(
      playedDifficulty.toString(),
    );

    if (scores.length > 50) {
      scores.removeAt(0);
    }

    if (accuracies.length > 50) {
      accuracies.removeAt(0);
    }

    if (reactions.length > 50) {
      reactions.removeAt(0);
    }

    if (difficulties.length > 50) {
      difficulties.removeAt(0);
    }

    await prefs.setStringList(
      'scores',
      scores,
    );

    await prefs.setStringList(
      'accuracies',
      accuracies,
    );

    await prefs.setStringList(
      'reactions',
      reactions,
    );

    await prefs.setStringList(
      'difficulties',
      difficulties,
    );

    await prefs.setInt(
      'difficulty',
      difficulty,
    );
  }

  // ==========================================================
  // NEXT ROUND
  // ==========================================================

  void nextRound() {

    currentRound++;

    startRound();
  }

  // ==========================================================
  // COLOR NAME
  // ==========================================================

  String colorName(Color color) {

    if (color == Colors.red) return 'RED';
    if (color == Colors.blue) return 'BLUE';
    if (color == Colors.green) return 'GREEN';
    if (color == Colors.orange) return 'ORANGE';
    if (color == Colors.purple) return 'PURPLE';
    if (color == Colors.teal) return 'TEAL';
    if (color == Colors.pink) return 'PINK';
    if (color == Colors.brown) return 'BROWN';

    return 'COLOR';
  }

  // ==========================================================
  // BUILD
  // ==========================================================

  @override
  Widget build(BuildContext context) {

    return Scaffold(

      appBar: AppBar(
        centerTitle: true,

        title: const Text(
          'Memory Sequence',
          style: TextStyle(
            fontSize: 23,
            fontWeight: FontWeight.bold,
          ),
        ),
      ),

      body: SafeArea(

        child: Padding(
          padding: const EdgeInsets.all(20),

          child: buildGameContent(),
        ),
      ),
    );
  }

  // ==========================================================
  // GAME CONTENT
  // ==========================================================

  Widget buildGameContent() {

    if (gameState == 'countdown') {

      return Center(

        child: Column(
          mainAxisAlignment:
              MainAxisAlignment.center,

          children: [

            const Text(
              'Get Ready!',
              style: TextStyle(
                fontSize: 32,
                fontWeight: FontWeight.bold,
              ),
            ),

            const SizedBox(height: 30),

            Text(
              '$countdown',
              style: const TextStyle(
                fontSize: 90,
                fontWeight: FontWeight.bold,
              ),
            ),

            const SizedBox(height: 20),

            Text(
              'Round $currentRound',
              style: const TextStyle(
                fontSize: 22,
              ),
            ),

            const SizedBox(height: 10),

            Text(
              'Difficulty: $difficulty blocks',
              style: const TextStyle(
                fontSize: 18,
              ),
            ),
          ],
        ),
      );
    }

    if (gameState == 'show') {

      return Center(

        child: Column(
          mainAxisAlignment:
              MainAxisAlignment.center,

          children: [

            const Text(
              'Remember This!',
              style: TextStyle(
                fontSize: 32,
                fontWeight: FontWeight.bold,
              ),
            ),

            const SizedBox(height: 15),

            const Text(
              'Remember the order of the colors.',
              style: TextStyle(
                fontSize: 19,
              ),
              textAlign: TextAlign.center,
            ),

            const SizedBox(height: 40),

            buildSequenceBlocks(),

            const SizedBox(height: 40),

            const Text(
              'Look carefully 👀',
              style: TextStyle(
                fontSize: 22,
                fontWeight: FontWeight.w600,
              ),
            ),
          ],
        ),
      );
    }

    if (gameState == 'recall') {

      return SingleChildScrollView(

        child: Column(

          children: [

            const SizedBox(height: 20),

            const Text(
              'Your Turn!',
              style: TextStyle(
                fontSize: 32,
                fontWeight: FontWeight.bold,
              ),
            ),

            const SizedBox(height: 10),

            const Text(
              'Tap the colors in the same order.',
              style: TextStyle(
                fontSize: 19,
              ),
              textAlign: TextAlign.center,
            ),

            const SizedBox(height: 25),

            Text(
              '${userSequence.length} / ${sequence.length}',
              style: const TextStyle(
                fontSize: 26,
                fontWeight: FontWeight.bold,
              ),
            ),

            const SizedBox(height: 20),

            buildUserSequence(),

            const SizedBox(height: 30),

            buildColorButtons(),

            const SizedBox(height: 20),

            if (mistakes > 0)
              Text(
                'Mistakes: $mistakes',
                style: const TextStyle(
                  fontSize: 19,
                  fontWeight: FontWeight.bold,
                ),
              ),
          ],
        ),
      );
    }

    if (gameState == 'result') {
      return buildResultScreen();
    }

    return const Center(
      child: CircularProgressIndicator(),
    );
  }

  // ==========================================================
  // SEQUENCE BLOCKS
  // ==========================================================

  Widget buildSequenceBlocks() {

    return Wrap(

      alignment:
          WrapAlignment.center,

      children: sequence.map(
        (color) {

          return Container(

            width: 70,
            height: 70,

            margin:
                const EdgeInsets.all(7),

            decoration: BoxDecoration(
              color: color,

              borderRadius:
                  BorderRadius.circular(16),

              boxShadow: const [

                BoxShadow(
                  blurRadius: 5,
                  offset:
                      Offset(0, 3),
                ),
              ],
            ),
          );
        },
      ).toList(),
    );
  }

  // ==========================================================
  // USER SEQUENCE
  // ==========================================================

  Widget buildUserSequence() {

    if (userSequence.isEmpty) {

      return Container(

        width: double.infinity,

        padding:
            const EdgeInsets.all(18),

        decoration: BoxDecoration(
          color:
              Colors.grey.shade100,

          borderRadius:
              BorderRadius.circular(15),
        ),

        child: const Text(
          'Your sequence will appear here',
          textAlign: TextAlign.center,

          style: TextStyle(
            fontSize: 17,
          ),
        ),
      );
    }

    return Wrap(

      alignment:
          WrapAlignment.center,

      children: userSequence
          .asMap()
          .entries
          .map(
        (entry) {

          final index =
              entry.key;

          final color =
              entry.value;

          return Container(

            width: 55,
            height: 55,

            margin:
                const EdgeInsets.all(5),

            alignment:
                Alignment.center,

            decoration:
                BoxDecoration(
              color: color,

              borderRadius:
                  BorderRadius.circular(12),
            ),

            child: Text(
              '${index + 1}',

              style: const TextStyle(
                color: Colors.white,
                fontSize: 20,
                fontWeight:
                    FontWeight.bold,
              ),
            ),
          );
        },
      ).toList(),
    );
  }

  // ==========================================================
  // COLOR BUTTONS
  // ==========================================================

  Widget buildColorButtons() {

    return GridView.count(

      crossAxisCount: 4,

      shrinkWrap: true,

      physics:
          const NeverScrollableScrollPhysics(),

      crossAxisSpacing: 12,
      mainAxisSpacing: 12,

      children: availableColors.map(
        (color) {

          return InkWell(

            borderRadius:
                BorderRadius.circular(18),

            onTap: () {
              selectColor(color);
            },

            child: Container(

              decoration:
                  BoxDecoration(
                color: color,

                borderRadius:
                    BorderRadius.circular(18),

                boxShadow: const [

                  BoxShadow(
                    blurRadius: 4,
                    offset:
                        Offset(0, 2),
                  ),
                ],
              ),

              child: Center(

                child: Text(
                  colorName(color),

                  style:
                      const TextStyle(
                    color:
                        Colors.white,

                    fontSize: 12,

                    fontWeight:
                        FontWeight.bold,
                  ),
                ),
              ),
            ),
          );
        },
      ).toList(),
    );
  }

  // ==========================================================
  // RESULT SCREEN
  // ==========================================================

  Widget buildResultScreen() {

    String message;
    String emoji;

    if (score >= 80) {

      emoji = '🌟';
      message = 'Excellent Memory!';

    } else if (score >= 50) {

      emoji = '👍';
      message = 'Good Job!';

    } else {

      emoji = '💪';
      message = 'Keep Practicing!';
    }

    return SingleChildScrollView(

      child: Center(

        child: Column(

          children: [

            const SizedBox(height: 25),

            Text(
              emoji,

              style: const TextStyle(
                fontSize: 70,
              ),
            ),

            const SizedBox(height: 10),

            Text(
              message,

              style: const TextStyle(
                fontSize: 30,
                fontWeight:
                    FontWeight.bold,
              ),
            ),

            const SizedBox(height: 10),

            Text(
              'Round $currentRound Complete',

              style: TextStyle(
                fontSize: 19,
                color:
                    Colors.grey.shade700,
              ),
            ),

            const SizedBox(height: 30),

            Container(

              width: double.infinity,

              padding:
                  const EdgeInsets.all(25),

              decoration:
                  BoxDecoration(
                color:
                    Colors.indigo.shade50,

                borderRadius:
                    BorderRadius.circular(22),
              ),

              child: Column(

                children: [

                  const Text(
                    'Memory Score',

                    style: TextStyle(
                      fontSize: 21,
                      fontWeight:
                          FontWeight.w600,
                    ),
                  ),

                  const SizedBox(height: 10),

                  Text(
                    '$score',

                    style: const TextStyle(
                      fontSize: 60,
                      fontWeight:
                          FontWeight.bold,
                    ),
                  ),

                  const Text(
                    'out of 100',

                    style: TextStyle(
                      fontSize: 17,
                    ),
                  ),
                ],
              ),
            ),

            const SizedBox(height: 20),

            performanceCard(
              icon: Icons.gps_fixed_rounded,
              title: 'Accuracy',
              value:
                  '${accuracy.toStringAsFixed(0)}%',
            ),

            performanceCard(
              icon: Icons.timer_rounded,
              title: 'Reaction Time',
              value:
                  '${reactionTime.toStringAsFixed(1)} sec',
            ),

            performanceCard(
              icon: Icons.error_outline_rounded,
              title: 'Mistakes',
              value: '$mistakes',
            ),

            performanceCard(
              icon: Icons.psychology_rounded,
              title: 'Next Difficulty',
              value:
                  '$difficulty blocks',
            ),

            const SizedBox(height: 20),

            Card(
              elevation: 3,
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(18),
              ),
              child: Padding(
                padding: const EdgeInsets.all(20),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Container(
                          padding: const EdgeInsets.all(10),
                          decoration: BoxDecoration(
                            borderRadius: BorderRadius.circular(12),
                            color: Theme.of(context)
                                .colorScheme
                                .primaryContainer,
                          ),
                          child: Icon(
                            Icons.psychology,
                            color: Theme.of(context)
                                .colorScheme
                                .onPrimaryContainer,
                            size: 28,
                          ),
                        ),
                        const SizedBox(width: 12),
                        const Expanded(
                          child: Text(
                            'AI Cognitive Analysis',
                            style: TextStyle(
                              fontSize: 19,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 18),
                    Text(
                      'Status: ${aiAnalysis["status"]}',
                      style: const TextStyle(
                        fontSize: 17,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                    const SizedBox(height: 10),
                    Text(
                      aiAnalysis["message"],
                      style: const TextStyle(
                        fontSize: 15,
                        height: 1.4,
                      ),
                    ),
                    const SizedBox(height: 14),
                    Container(
                      width: double.infinity,
                      padding: const EdgeInsets.all(14),
                      decoration: BoxDecoration(
                        borderRadius: BorderRadius.circular(12),
                        color: Colors.grey.shade100,
                      ),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          const Text(
                            'Recommended for you',
                            style: TextStyle(
                              fontWeight: FontWeight.bold,
                              fontSize: 15,
                            ),
                          ),
                          const SizedBox(height: 6),
                          Text(
                            aiAnalysis["recommendation"],
                            style: const TextStyle(
                              fontSize: 14,
                              height: 1.4,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ),
            ),

            const SizedBox(height: 20),

            Container(

              width: double.infinity,

              padding:
                  const EdgeInsets.all(18),

              decoration:
                  BoxDecoration(
                color:
                    Colors.green.shade50,

                borderRadius:
                    BorderRadius.circular(18),

                border: Border.all(
                  color:
                      Colors.green.shade200,
                ),
              ),

              child: Row(

                children: [

                  const Text(
                    '🤖',

                    style: TextStyle(
                      fontSize: 30,
                    ),
                  ),

                  const SizedBox(width: 12),

                  Expanded(
                    child: Text(
                      getAdaptiveMessage(),

                      style:
                          const TextStyle(
                        fontSize: 17,
                        fontWeight:
                            FontWeight.w600,
                      ),
                    ),
                  ),
                ],
              ),
            ),

            const SizedBox(height: 30),

            SizedBox(
              width: double.infinity,
              height: 60,

              child: ElevatedButton(

                onPressed: nextRound,

                child: const Text(
                  'NEXT ROUND',

                  style: TextStyle(
                    fontSize: 21,
                    fontWeight:
                        FontWeight.bold,
                  ),
                ),
              ),
            ),

            const SizedBox(height: 12),

            SizedBox(
              width: double.infinity,
              height: 55,

              child: OutlinedButton(

                onPressed: () {
                  Navigator.pop(context);
                },

                child: const Text(
                  'BACK TO HOME',

                  style: TextStyle(
                    fontSize: 19,
                    fontWeight:
                        FontWeight.bold,
                  ),
                ),
              ),
            ),

            const SizedBox(height: 30),
          ],
        ),
      ),
    );
  }

  // ==========================================================
  // PERFORMANCE CARD
  // ==========================================================

  Widget performanceCard({
    required IconData icon,
    required String title,
    required String value,
  }) {

    return Card(

      margin:
          const EdgeInsets.only(
        bottom: 10,
      ),

      child: ListTile(

        contentPadding:
            const EdgeInsets.symmetric(
          horizontal: 18,
          vertical: 5,
        ),

        leading: Icon(
          icon,
          size: 28,
          color: Colors.indigo,
        ),

        title: Text(
          title,

          style: const TextStyle(
            fontSize: 18,
            fontWeight:
                FontWeight.w600,
          ),
        ),

        trailing: Text(
          value,

          style: const TextStyle(
            fontSize: 18,
            fontWeight:
                FontWeight.bold,
          ),
        ),
      ),
    );
  }

  // ==========================================================
  // ADAPTIVE MESSAGE
  // ==========================================================

  String getAdaptiveMessage() {

    if (score >= 80) {

      return
          'Excellent performance! The next session will be '
          'slightly more challenging.';

    } else if (score < 50) {

      return
          'The difficulty has been reduced to provide a '
          'comfortable challenge.';

    } else {

      return
          'Good progress! Your current difficulty will be maintained.';
    }
  }

  // ==========================================================
  // DISPOSE
  // ==========================================================

  @override
  void dispose() {

    countdownTimer?.cancel();

    stopwatch.stop();

    super.dispose();
  }
}

// ============================================================
// VOICE ASSISTANT PAGE
// ============================================================

class VoiceAssistantPage extends StatefulWidget {
  const VoiceAssistantPage({super.key});

  @override
  State<VoiceAssistantPage> createState() => _VoiceAssistantPageState();
}

class _VoiceAssistantPageState extends State<VoiceAssistantPage> {
  final SpeechToText speechToText = SpeechToText();
  final FlutterTts flutterTts = FlutterTts();

  bool isListening = false;
  String recognizedText = "Tap the microphone and speak";
  String response = "Hello! How can I help you today?";

  Future<String> describeReminders() async {
    final prefs = await SharedPreferences.getInstance();
    final saved = prefs.getStringList('reminders') ?? [];

    if (saved.isEmpty) {
      return "You have no reminders set at the moment.";
    }

    final parts = saved.map((entry) {
      final bits = entry.split('|');
      return '${bits[0]} at ${bits.length > 1 ? bits[1] : ""}';
    }).join(', ');

    return "You have ${saved.length} reminder${saved.length == 1 ? '' : 's'}: $parts.";
  }

  Future<void> speak(String text) async {
    await flutterTts.setLanguage("en-IN");
    await flutterTts.setSpeechRate(0.45);
    await flutterTts.setPitch(1.0);
    await flutterTts.speak(text);
  }

  Future<void> startListening() async {
    final available = await speechToText.initialize();

    if (!available) {
      setState(() {
        response = "Voice recognition is not available on this device.";
      });
      return;
    }

    setState(() {
      isListening = true;
      recognizedText = "Listening...";
    });

    await speechToText.listen(
      localeId: 'en_IN',
      listenFor: const Duration(seconds: 30),
      pauseFor: const Duration(seconds: 3),
      partialResults: true,
      onResult: (result) {
        setState(() {
          recognizedText = result.recognizedWords;
        });

        if (result.finalResult) {
          processCommand(result.recognizedWords);
        }
      },
    );
  }

  Future<void> stopListening() async {
    await speechToText.stop();

    setState(() {
      isListening = false;
    });
  }

  Future<void> processCommand(String command) async {
    final text = command.toLowerCase();

    final isMemoryCommand =
        text.contains("memory") ||
        text.contains("exercise") ||
        text.contains("game") ||
        text.contains("yaad") ||
        text.contains("dimaag") ||
        text.contains("khel");

    final isReminderCommand =
        text.contains("reminder") ||
        text.contains("reminders") ||
        text.contains("yaad dilao") ||
        text.contains("yaad dilana");

    final isMedicineCommand =
        text.contains("medicine") ||
        text.contains("medication") ||
        text.contains("dawai") ||
        text.contains("dawa");

    final isPerformanceCommand =
        text.contains("performance") ||
        text.contains("score") ||
        text.contains("today") ||
        text.contains("performance batao");

    String newResponse;

    if (isMemoryCommand) {
      newResponse = "Sure! Let's start your memory exercise.";

      setState(() {
        response = newResponse;
        isListening = false;
      });

      speak(newResponse);

      Future.delayed(const Duration(milliseconds: 700), () {
        if (!mounted) return;

        Navigator.push(
          context,
          MaterialPageRoute(
            builder: (context) => const MemoryGamePage(),
          ),
        );
      });

      return;
    } else if (isReminderCommand) {
      newResponse = await describeReminders();
    } else if (isMedicineCommand) {
      newResponse = await describeReminders();
    } else if (isPerformanceCommand) {
      newResponse =
          "Your recent cognitive performance is being monitored. Keep practicing your memory exercises.";
    } else if (text.contains("hello") ||
        text.trim() == "hi" ||
        text.startsWith("hi ")) {
      newResponse = "Hello! It is nice to hear from you. How can I help?";
    } else {
      newResponse =
          "I heard you say: $command. I am still learning how to help with that.";
    }

    setState(() {
      response = newResponse;
      isListening = false;
    });

    speak(newResponse);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text(
          "Talk to SMRITI+",
          style: TextStyle(fontWeight: FontWeight.bold),
        ),
      ),
      body: Center(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(24),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Icon(
                Icons.record_voice_over,
                size: 80,
                color: Colors.blue,
              ),
              const SizedBox(height: 25),
              const Text(
                "How can I help you?",
                style: TextStyle(
                  fontSize: 28,
                  fontWeight: FontWeight.bold,
                ),
              ),
              const SizedBox(height: 20),
              Text(
                recognizedText,
                textAlign: TextAlign.center,
                style: const TextStyle(
                  fontSize: 19,
                ),
              ),
              const SizedBox(height: 30),
              GestureDetector(
                onTap: isListening ? stopListening : startListening,
                child: Container(
                  width: 110,
                  height: 110,
                  decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    color: isListening ? Colors.red : Colors.blue,
                  ),
                  child: Icon(
                    isListening ? Icons.stop : Icons.mic,
                    color: Colors.white,
                    size: 55,
                  ),
                ),
              ),
              const SizedBox(height: 30),
              Card(
                elevation: 3,
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(18),
                ),
                child: Padding(
                  padding: const EdgeInsets.all(20),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      const Row(
                        children: [
                          Icon(
                            Icons.smart_toy,
                            size: 28,
                          ),
                          SizedBox(width: 10),
                          Text(
                            "SMRITI+ Assistant",
                            style: TextStyle(
                              fontSize: 18,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 15),
                      Text(
                        response,
                        style: const TextStyle(
                          fontSize: 16,
                          height: 1.5,
                        ),
                      ),
                    ],
                  ),
                ),
              ),
              const SizedBox(height: 25),
              const Text(
                "Try saying:",
                style: TextStyle(
                  fontSize: 17,
                  fontWeight: FontWeight.bold,
                ),
              ),
              const SizedBox(height: 10),
              const Text(
                "\"Start my memory exercise\"\n"
                "\"What are my reminders?\"\n"
                "\"When is my medicine?\"\n"
                "\"How did I perform today?\"",
                textAlign: TextAlign.center,
                style: TextStyle(
                  fontSize: 15,
                  height: 1.7,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  @override
  void dispose() {
    speechToText.stop();
    super.dispose();
  }
}