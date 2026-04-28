import '../../../core/constants/api_constants.dart';
import '../../../core/network/dio_client.dart';
import 'models/card_model.dart';

class CardApiService {
  final DioClient _client;

  CardApiService() : _client = DioClient(ApiConstants.cardBaseUrl);

  Future<CardModel> createCard({
    required String accountId,
    required String customerId,
    required String cardType,
    required String pin,
  }) async {
    final response = await _client.post(
      '${ApiConstants.cards}/create',
      data: {
        'accountId': accountId,
        'customerId': customerId,
        'cardType': cardType,
        'pin': pin,
      },
    );
    return CardModel.fromJson(response.data);
  }

  Future<CardModel> getCardById(String cardId) async {
    final response = await _client.get('${ApiConstants.cards}/$cardId');
    return CardModel.fromJson(response.data);
  }

  Future<List<CardModel>> getCardsByCustomerId(String customerId) async {
    final response = await _client.get(
      '${ApiConstants.cards}/customer/$customerId',
    );
    return (response.data as List)
        .map((e) => CardModel.fromJson(e))
        .toList();
  }

  Future<CardModel> blockCard(String cardId) async {
    final response = await _client.patch(
      '${ApiConstants.cards}/$cardId/block',
    );
    return CardModel.fromJson(response.data);
  }

  Future<CardModel> updatePin(String cardId, String newPin) async {
    final response = await _client.patch(
      '${ApiConstants.cards}/$cardId/update-pin',
      queryParameters: {'newPin': newPin},
    );
    return CardModel.fromJson(response.data);
  }

  Future<CardModel> updateCardStatus(String cardId, String status) async {
    final response = await _client.patch(
      '${ApiConstants.cards}/$cardId/status',
      queryParameters: {'status': status},
    );
    return CardModel.fromJson(response.data);
  }

  Future<void> deleteCard(String cardId) async {
    await _client.delete('${ApiConstants.cards}/$cardId');
  }
}
