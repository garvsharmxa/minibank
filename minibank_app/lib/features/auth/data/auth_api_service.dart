import '../../../core/constants/api_constants.dart';
import '../../../core/network/dio_client.dart';
import 'models/auth_response.dart';
import 'models/login_request.dart';
import 'models/register_request.dart';
import 'models/user_model.dart';

class AuthApiService {
  final DioClient _client;

  AuthApiService() : _client = DioClient(ApiConstants.authBaseUrl);

  Future<Map<String, dynamic>> register(RegisterRequest request) async {
    final response = await _client.post(
      ApiConstants.register,
      data: request.toJson(),
    );
    return response.data;
  }

  Future<AuthResponseModel> login(LoginRequest request) async {
    final response = await _client.post(
      ApiConstants.login,
      data: request.toJson(),
    );
    // Auth service wraps response in ApiResponse<AuthResponse>
    final data = response.data;
    if (data['success'] == true && data['data'] != null) {
      return AuthResponseModel.fromJson(data['data']);
    }
    throw Exception(data['message'] ?? 'Login failed');
  }

  Future<AuthResponseModel> refreshToken(String refreshToken) async {
    final response = await _client.post(
      ApiConstants.refreshToken,
      data: {'refreshToken': refreshToken},
    );
    final data = response.data;
    if (data['success'] == true && data['data'] != null) {
      return AuthResponseModel.fromJson(data['data']);
    }
    throw Exception(data['message'] ?? 'Token refresh failed');
  }

  Future<void> logout(String? refreshToken) async {
    await _client.post(
      ApiConstants.logout,
      data: refreshToken != null ? {'refreshToken': refreshToken} : null,
    );
  }

  Future<void> changePassword(
    String currentPassword,
    String newPassword,
  ) async {
    final response = await _client.post(
      ApiConstants.changePassword,
      data: {'currentPassword': currentPassword, 'newPassword': newPassword},
    );
    final data = response.data;
    if (data['success'] != true) {
      throw Exception(data['message'] ?? 'Password change failed');
    }
  }

  Future<UserModel> getUserById(String userId) async {
    final response = await _client.get('${ApiConstants.users}/$userId');
    final data = response.data;
    if (data is Map<String, dynamic> && data.containsKey('data')) {
      return UserModel.fromJson(data['data']);
    }
    return UserModel.fromJson(data);
  }
}
