import '../../../core/constants/api_constants.dart';
import '../../../core/network/dio_client.dart';
import 'models/transaction_model.dart';

class TransactionApiService {
  final DioClient _client;

  TransactionApiService() : _client = DioClient(ApiConstants.transactionBaseUrl);

  Future<TransactionModel> createTransaction(
    String accountId,
    TransactionModel transaction,
  ) async {
    final response = await _client.post(
      '${ApiConstants.transactions}/create/$accountId',
      data: transaction.toJson(),
    );
    return TransactionModel.fromJson(response.data);
  }

  Future<TransactionModel> getTransactionById(String transactionId) async {
    final response = await _client.get(
      '${ApiConstants.transactions}/$transactionId',
    );
    return TransactionModel.fromJson(response.data);
  }

  Future<List<TransactionModel>> getAllTransactions() async {
    final response = await _client.get('${ApiConstants.transactions}/all');
    return (response.data as List)
        .map((e) => TransactionModel.fromJson(e))
        .toList();
  }

  Future<List<TransactionModel>> getTransactionsByAccountId(
    String accountId,
  ) async {
    final response = await _client.get(
      '${ApiConstants.transactions}/account/$accountId',
    );
    return (response.data as List)
        .map((e) => TransactionModel.fromJson(e))
        .toList();
  }

  Future<List<TransactionModel>> getTransactionsByCustomerId(
    String customerId,
  ) async {
    final response = await _client.get(
      '${ApiConstants.transactions}/customer/$customerId',
    );
    return (response.data as List)
        .map((e) => TransactionModel.fromJson(e))
        .toList();
  }

  Future<List<TransactionModel>> getTransactionsByStatus(String status) async {
    final response = await _client.get(
      '${ApiConstants.transactions}/status/$status',
    );
    return (response.data as List)
        .map((e) => TransactionModel.fromJson(e))
        .toList();
  }
}
