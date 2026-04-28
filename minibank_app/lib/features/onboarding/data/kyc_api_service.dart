import 'package:dio/dio.dart';
import '../../../core/constants/api_constants.dart';
import '../../../core/network/dio_client.dart';
import 'models/kyc_status_model.dart';

class KycApiService {
  final DioClient _client;

  KycApiService() : _client = DioClient(ApiConstants.kycBaseUrl);

  /// Submit KYC — uses multipart form data (matching backend @RequestParam)
  Future<Map<String, dynamic>> submitKyc({
    required String customerId,
    required String panNumber,
    required String aadhaarNumber,
  }) async {
    final formData = FormData.fromMap({
      'customerId': customerId,
      'panNumber': panNumber,
      'aadharNumber': aadhaarNumber,
    });

    final response = await _client.post(
      '${ApiConstants.kyc}/create',
      data: formData,
      options: Options(contentType: 'multipart/form-data'),
    );
    return response.data;
  }

  /// Get KYC status (lightweight) for customer
  Future<KycStatusModel> getKycStatus(String customerId) async {
    final response = await _client.get(
      '${ApiConstants.kyc}/customer/$customerId/status',
    );
    return KycStatusModel.fromJson(response.data);
  }

  /// Check if KYC is verified
  Future<bool> isKycVerified(String customerId) async {
    try {
      final response = await _client.get(
        '${ApiConstants.kyc}/customer/$customerId/verified',
      );
      return response.data == true;
    } catch (_) {
      return false;
    }
  }
}
