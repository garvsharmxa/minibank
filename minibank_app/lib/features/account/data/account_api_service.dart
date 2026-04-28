import '../../../core/constants/api_constants.dart';
import '../../../core/network/dio_client.dart';
import 'models/account_model.dart';

class AccountApiService {
  final DioClient _client;

  AccountApiService() : _client = DioClient(ApiConstants.accountBaseUrl);

  Future<List<AccountModel>> getAccountsByCustomerId(String customerId) async {
    final response = await _client.get('${ApiConstants.accounts}/customer/$customerId');
    final data = response.data;

    if (data is List) {
      return data.map((e) => AccountModel.fromJson(e)).toList();
    }
    // Single account returned
    if (data is Map<String, dynamic>) {
      return [AccountModel.fromJson(data)];
    }
    return [];
  }

  Future<AccountModel> getAccountById(String accountId) async {
    final response = await _client.get('${ApiConstants.accounts}/$accountId');
    return AccountModel.fromJson(response.data);
  }

  Future<AccountModel> createAccount({
    required String customerId,
    required String accountType,
    double balance = 0.0,
    String currency = 'USD',
  }) async {
    final response = await _client.post(
      ApiConstants.accounts,
      data: {
        'customerId': customerId,
        'accountType': accountType,
        'balance': balance,
        'currency': currency,
      },
    );
    return AccountModel.fromJson(response.data);
  }

  Future<AccountModel> deposit(String accountId, double amount) async {
    final response = await _client.post(
      '${ApiConstants.accounts}/$accountId/deposit',
      queryParameters: {'amount': amount},
    );
    return AccountModel.fromJson(response.data);
  }

  Future<AccountModel> withdraw(String accountId, double amount) async {
    final response = await _client.post(
      '${ApiConstants.accounts}/$accountId/withdraw',
      queryParameters: {'amount': amount},
    );
    return AccountModel.fromJson(response.data);
  }

  Future<AccountModel> blockAccount(String accountId) async {
    final response = await _client.put(
      '${ApiConstants.accounts}/$accountId/block',
    );
    return AccountModel.fromJson(response.data);
  }

  Future<AccountModel> activateAccount(String accountId) async {
    final response = await _client.put(
      '${ApiConstants.accounts}/$accountId/activate',
    );
    return AccountModel.fromJson(response.data);
  }

  Future<void> deleteAccount(String accountId) async {
    await _client.delete('${ApiConstants.accounts}/$accountId');
  }
}
