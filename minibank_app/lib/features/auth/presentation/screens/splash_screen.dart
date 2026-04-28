import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../../core/theme/app_colors.dart';
import '../../providers/auth_provider.dart';
import '../../../onboarding/providers/onboarding_provider.dart';

class SplashScreen extends ConsumerStatefulWidget {
  const SplashScreen({super.key});

  @override
  ConsumerState<SplashScreen> createState() => _SplashScreenState();
}

class _SplashScreenState extends ConsumerState<SplashScreen>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late Animation<double> _fadeAnimation;
  late Animation<double> _scaleAnimation;

  @override
  void initState() {
    super.initState();
    _controller = AnimationController(
      duration: const Duration(milliseconds: 1500),
      vsync: this,
    );

    _fadeAnimation = Tween<double>(begin: 0.0, end: 1.0).animate(
      CurvedAnimation(parent: _controller, curve: Curves.easeInOut),
    );

    _scaleAnimation = Tween<double>(begin: 0.5, end: 1.0).animate(
      CurvedAnimation(parent: _controller, curve: Curves.elasticOut),
    );

    _controller.forward();

    Future.delayed(const Duration(seconds: 2), () {
      _navigate();
    });
  }

  Future<void> _navigate() async {
    if (!mounted) return;

    // Check if user is logged in
    await ref.read(authProvider.notifier).checkAuthStatus();
    final authState = ref.read(authProvider);

    if (!authState.isAuthenticated) {
      if (mounted) context.go('/login');
      return;
    }

    // User is authenticated — check onboarding status
    final email = authState.email ??
        await ref.read(authProvider.notifier).getStoredEmail() ??
        authState.username ??
        '';

    if (email.isEmpty) {
      if (mounted) context.go('/dashboard');
      return;
    }

    try {
      final step = await ref
          .read(onboardingProvider.notifier)
          .checkOnboardingStatus(email);

      if (!mounted) return;

      switch (step) {
        case OnboardingStep.customerProfile:
          context.go('/create-customer');
          break;
        case OnboardingStep.kycSubmission:
          context.go('/kyc');
          break;
        case OnboardingStep.kycPending:
          context.go('/kyc-pending');
          break;
        case OnboardingStep.complete:
          context.go('/dashboard');
          break;
        case OnboardingStep.checking:
          context.go('/dashboard');
          break;
      }
    } catch (_) {
      // If onboarding check fails, go to dashboard as fallback
      if (mounted) context.go('/dashboard');
    }
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.primary,
      body: Center(
        child: AnimatedBuilder(
          animation: _controller,
          builder: (context, child) {
            return FadeTransition(
              opacity: _fadeAnimation,
              child: ScaleTransition(
                scale: _scaleAnimation,
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    Container(
                      width: 100,
                      height: 100,
                      decoration: BoxDecoration(
                        color: AppColors.surface,
                        borderRadius: BorderRadius.circular(24),
                      ),
                      child: const Icon(
                        Icons.account_balance_rounded,
                        size: 56,
                        color: AppColors.primary,
                      ),
                    ),
                    const SizedBox(height: 24),
                    Text(
                      'MiniBank',
                      style: Theme.of(context).textTheme.displayMedium?.copyWith(
                            color: AppColors.textOnPrimary,
                            fontWeight: FontWeight.w700,
                          ),
                    ),
                    const SizedBox(height: 8),
                    Text(
                      'Your Digital Banking Partner',
                      style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                            color: AppColors.textOnPrimary.withValues(alpha: 0.7),
                          ),
                    ),
                  ],
                ),
              ),
            );
          },
        ),
      ),
    );
  }
}
