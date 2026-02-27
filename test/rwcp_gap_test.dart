import 'package:fake_async/fake_async.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:gaia/utils/gaia/rwcp/rwcp.dart';
import 'package:gaia/utils/gaia/rwcp/rwcp_client.dart';
import 'package:gaia/utils/gaia/rwcp/rwcp_listener.dart';
import 'package:gaia/utils/gaia/rwcp/segment.dart';

class _TestRwcpListener implements RWCPListener {
  final sentSegments = <Segment>[];

  @override
  bool sendRWCPSegment(List<int> bytes) {
    sentSegments.add(Segment.parse(bytes));
    return true;
  }

  @override
  void onTransferFailed() {}

  @override
  void onTransferFinished() {}

  @override
  void onTransferProgress(int acknowledged) {}
}

void main() {
  test('GAP 会重传缺口起点对应的数据段', () {
    fakeAsync((async) {
      final listener = _TestRwcpListener();
      final client = RWCPClient(listener)
        ..showDebugLogs(false)
        ..setCloseSessionWhenIdle(false);

      // 预先塞入 3 个 DATA，确保建立会话后会发出 seq=1/2/3 三个段。
      client.sendData([0xA1]);
      client.sendData([0xA2]);
      client.sendData([0xA3]);

      // 完成 RST/SYN 握手：进入 ESTABLISHED 并发送 DATA 段。
      client.onReceiveRWCPSegment(
        Segment.get(RWCPOpCodeServer.rstAck, 0).getBytes(),
      );
      client.onReceiveRWCPSegment(
        Segment.get(RWCPOpCodeServer.synAck, 0).getBytes(),
      );

      final sentDataSequences = listener.sentSegments
          .where((s) => s.getOperationCode() == RWCPOpCodeClient.data)
          .map((s) => s.getSequenceNumber())
          .toList();
      expect(sentDataSequences, containsAll(<int>[1, 2, 3]));

      // 设备提示 seq=2 出现缺口（下一期待/缺口起点），客户端应重传 seq=2（以及其后的未确认段）。
      final beforeGap = listener.sentSegments.length;
      client.onReceiveRWCPSegment(
        Segment.get(RWCPOpCodeServer.gap, 2).getBytes(),
      );

      final resentDataSequences = listener.sentSegments
          .sublist(beforeGap)
          .where((s) => s.getOperationCode() == RWCPOpCodeClient.data)
          .map((s) => s.getSequenceNumber())
          .toList();
      expect(resentDataSequences, <int>[2, 3]);

      client.dispose();
    });
  });
}

