import 'package:flutter_test/flutter_test.dart';
import 'package:smriti_plus/main.dart';

void main() {
  testWidgets('SMRITI+ app loads', (WidgetTester tester) async {
    await tester.pumpWidget(const SmritiApp());

    expect(find.text('SMRITI+'), findsOneWidget);
  });
}
