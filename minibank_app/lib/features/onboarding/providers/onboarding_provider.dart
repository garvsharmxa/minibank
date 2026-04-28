import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../data/customer_api_service.dart';
import '../data/kyc_api_service.dart';
import '../data/models/customer_model.dart';
import '../data/models/kyc_status_model.dart';

enum OnboardingStep {
  checking,
  customerProfile,
  kycSubmission,
  kycPending,
  complete,
}

@immutable
class OnboardingState {
  final bool isLoading;
  final OnboardingStep step;
  final CustomerModel? customer;
  final KycStatusModel? kycStatus;
  final String? error;
  final String? customerId;

  const OnboardingState({
    this.isLoading = false,
    this.step = OnboardingStep.checking,
    this.customer,
    this.kycStatus,
    this.error,
    this.customerId,
  });

  OnboardingState copyWith({
    bool? isLoading,
    OnboardingStep? step,
    CustomerModel? customer,
    KycStatusModel? kycStatus,
    String? error,
    String? customerId,
  }) {
    return OnboardingState(
      isLoading: isLoading ?? this.isLoading,
      step: step ?? this.step,
      customer: customer ?? this.customer,
      kycStatus: kycStatus ?? this.kycStatus,
      error: error,
      customerId: customerId ?? this.customerId,
    );
  }
}

class OnboardingNotifier extends StateNotifier<OnboardingState> {
  final CustomerApiService _customerService;
  final KycApiService _kycService;

  OnboardingNotifier()
      : _customerService = CustomerApiService(),
        _kycService = KycApiService(),
        super(const OnboardingState());

  /// Check the full onboarding status for a user by email.
  /// Returns the correct onboarding step.
  Future<OnboardingStep> checkOnboardingStatus(String email) async {
    state = state.copyWith(isLoading: true, error: null);
    try {
      // Step 1: Check if customer profile exists
      final customer = await _customerService.getCustomerByEmail(email);
      state = state.copyWith(
        customer: customer,
        customerId: customer.id,
      );

      // Step 2: Check KYC status
      final kycStatus = await _kycService.getKycStatus(customer.id);
      state = state.copyWith(kycStatus: kycStatus);

      if (!kycStatus.exists) {
        state = state.copyWith(
          isLoading: false,
          step: OnboardingStep.kycSubmission,
        );
        return OnboardingStep.kycSubmission;
      }

      if (!kycStatus.verified) {
        state = state.copyWith(
          isLoading: false,
          step: OnboardingStep.kycPending,
        );
        return OnboardingStep.kycPending;
      }

      // Everything complete
      state = state.copyWith(
        isLoading: false,
        step: OnboardingStep.complete,
      );
      return OnboardingStep.complete;
    } catch (e) {
      // If customer not found (404), need to create customer profile
      if (e.toString().contains('404') || e.toString().contains('not found')) {
        state = state.copyWith(
          isLoading: false,
          step: OnboardingStep.customerProfile,
        );
        return OnboardingStep.customerProfile;
      }
      state = state.copyWith(
        isLoading: false,
        step: OnboardingStep.customerProfile,
        error: _parseError(e),
      );
      return OnboardingStep.customerProfile;
    }
  }

  /// Create customer profile
  Future<bool> createCustomer({
    required String name,
    required String email,
    required String phone,
    String? address,
    String? city,
    String? customerState,
    String? zip,
  }) async {
    state = state.copyWith(isLoading: true, error: null);
    try {
      final customer = await _customerService.createCustomer({
        'name': name,
        'email': email,
        'phone': phone,
        'address': address ?? '',
        'city': city ?? '',
        'state': customerState ?? '',
        'zip': zip ?? '',
      });
      state = state.copyWith(
        isLoading: false,
        customer: customer,
        customerId: customer.id,
        step: OnboardingStep.kycSubmission,
      );
      return true;
    } catch (e) {
      state = state.copyWith(
        isLoading: false,
        error: _parseError(e),
      );
      return false;
    }
  }

  /// Submit KYC
  Future<bool> submitKyc({
    required String panNumber,
    required String aadhaarNumber,
  }) async {
    if (state.customerId == null) {
      state = state.copyWith(error: 'Customer profile not found');
      return false;
    }
    state = state.copyWith(isLoading: true, error: null);
    try {
      await _kycService.submitKyc(
        customerId: state.customerId!,
        panNumber: panNumber,
        aadhaarNumber: aadhaarNumber,
      );
      state = state.copyWith(
        isLoading: false,
        step: OnboardingStep.kycPending,
      );
      return true;
    } catch (e) {
      state = state.copyWith(
        isLoading: false,
        error: _parseError(e),
      );
      return false;
    }
  }

  /// Check if KYC has been verified by admin
  Future<bool> checkKycVerification() async {
    if (state.customerId == null) return false;
    state = state.copyWith(isLoading: true, error: null);
    try {
      final verified = await _kycService.isKycVerified(state.customerId!);
      if (verified) {
        state = state.copyWith(
          isLoading: false,
          step: OnboardingStep.complete,
        );
        return true;
      }
      state = state.copyWith(isLoading: false);
      return false;
    } catch (e) {
      state = state.copyWith(
        isLoading: false,
        error: _parseError(e),
      );
      return false;
    }
  }

  /// Set the customer ID directly (e.g. from stored value)
  void setCustomerId(String customerId) {
    state = state.copyWith(customerId: customerId);
  }

  String _parseError(dynamic error) {
    final msg = error.toString();
    if (msg.contains('DioException')) {
      if (msg.contains('connection')) {
        return 'Unable to connect to server. Check your connection.';
      }
      // Try to extract backend message
      if (msg.contains('already exists')) {
        return msg.split('already exists').first.split(':').last.trim() +
            ' already exists';
      }
      return 'Server error. Please try again.';
    }
    return msg.replaceAll('Exception: ', '');
  }
}

final onboardingProvider =
    StateNotifierProvider<OnboardingNotifier, OnboardingState>((ref) {
  return OnboardingNotifier();
});
