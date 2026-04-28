import '../../../core/constants/api_constants.dart';
import '../../../core/network/dio_client.dart';
import 'models/customer_model.dart';

class CustomerApiService {
  final DioClient _client;

  CustomerApiService() : _client = DioClient(ApiConstants.customerBaseUrl);

  Future<CustomerModel> createCustomer(Map<String, dynamic> data) async {
    final response = await _client.post(
      ApiConstants.customers,
      data: data,
    );
    return CustomerModel.fromJson(response.data);
  }

  Future<CustomerModel> getCustomerByEmail(String email) async {
    final response = await _client.get('${ApiConstants.customers}/email/$email');
    return CustomerModel.fromJson(response.data);
  }

  Future<CustomerModel> getCustomerById(String customerId) async {
    final response = await _client.get('${ApiConstants.customers}/$customerId');
    return CustomerModel.fromJson(response.data);
  }

  Future<bool> customerExists(String customerId) async {
    try {
      final response = await _client.get('${ApiConstants.customers}/$customerId/exists');
      return response.data == true;
    } catch (_) {
      return false;
    }
  }
}
