import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import '../../../../core/theme/app_colors.dart';

class MainShell extends StatelessWidget {
  final Widget child;

  const MainShell({super.key, required this.child});

  static int _currentIndex(BuildContext context) {
    final location = GoRouterState.of(context).uri.toString();
    if (location.startsWith('/accounts')) return 1;
    if (location.startsWith('/transactions')) return 2;
    if (location.startsWith('/cards')) return 3;
    if (location.startsWith('/profile')) return 4;
    return 0;
  }

  @override
  Widget build(BuildContext context) {
    final currentIndex = _currentIndex(context);

    return Scaffold(
      body: child,
      bottomNavigationBar: Container(
        decoration: BoxDecoration(
          color: AppColors.surface,
          border: Border(
            top: BorderSide(color: AppColors.border, width: 1),
          ),
        ),
        child: SafeArea(
          child: Padding(
            padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 8),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceAround,
              children: [
                _buildNavItem(context, 0, Icons.home_rounded, Icons.home_outlined, 'Home', currentIndex),
                _buildNavItem(context, 1, Icons.account_balance_wallet_rounded, Icons.account_balance_wallet_outlined, 'Accounts', currentIndex),
                _buildNavItem(context, 2, Icons.receipt_long_rounded, Icons.receipt_long_outlined, 'History', currentIndex),
                _buildNavItem(context, 3, Icons.credit_card_rounded, Icons.credit_card_outlined, 'Cards', currentIndex),
                _buildNavItem(context, 4, Icons.person_rounded, Icons.person_outline_rounded, 'Profile', currentIndex),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildNavItem(
    BuildContext context,
    int index,
    IconData activeIcon,
    IconData inactiveIcon,
    String label,
    int currentIndex,
  ) {
    final isActive = index == currentIndex;

    return GestureDetector(
      onTap: () {
        switch (index) {
          case 0:
            context.go('/dashboard');
            break;
          case 1:
            context.go('/accounts');
            break;
          case 2:
            context.go('/transactions');
            break;
          case 3:
            context.go('/cards');
            break;
          case 4:
            context.go('/profile');
            break;
        }
      },
      behavior: HitTestBehavior.opaque,
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 200),
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
        decoration: BoxDecoration(
          color: isActive ? AppColors.primary.withValues(alpha: 0.08) : Colors.transparent,
          borderRadius: BorderRadius.circular(12),
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(
              isActive ? activeIcon : inactiveIcon,
              color: isActive ? AppColors.primary : AppColors.textLight,
              size: 24,
            ),
            const SizedBox(height: 4),
            Text(
              label,
              style: TextStyle(
                fontSize: 11,
                fontWeight: isActive ? FontWeight.w600 : FontWeight.w400,
                color: isActive ? AppColors.primary : AppColors.textLight,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
