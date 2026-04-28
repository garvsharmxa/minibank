import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/widgets/app_button.dart';
import '../../../../core/widgets/app_text_field.dart';
import '../../../../core/utils/validators.dart';
import '../../../auth/providers/auth_provider.dart';

class ProfileScreen extends ConsumerWidget {
  const ProfileScreen({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final authState = ref.watch(authProvider);

    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: AppBar(
        title: const Text('Profile'),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(20),
        child: Column(
          children: [
            // Profile Avatar
            Container(
              width: 90,
              height: 90,
              decoration: BoxDecoration(
                color: AppColors.primary,
                shape: BoxShape.circle,
              ),
              child: Center(
                child: Text(
                  (authState.username ?? 'U')[0].toUpperCase(),
                  style: Theme.of(context).textTheme.displayMedium?.copyWith(
                        color: AppColors.textOnPrimary,
                        fontWeight: FontWeight.w700,
                      ),
                ),
              ),
            ),
            const SizedBox(height: 14),
            Text(
              authState.username ?? 'User',
              style: Theme.of(context).textTheme.headlineMedium,
            ),
            const SizedBox(height: 4),
            Text(
              'MiniBank Customer',
              style: Theme.of(context).textTheme.bodyMedium,
            ),

            const SizedBox(height: 32),

            // Menu Items
            _buildSection(context, [
              _MenuItem(
                icon: Icons.person_outline_rounded,
                title: 'Personal Information',
                onTap: () {},
              ),
              _MenuItem(
                icon: Icons.verified_user_outlined,
                title: 'KYC Verification',
                subtitle: 'Complete your verification',
                onTap: () {},
              ),
              _MenuItem(
                icon: Icons.account_balance_outlined,
                title: 'Bank Accounts',
                onTap: () {},
              ),
            ]),

            const SizedBox(height: 16),

            _buildSection(context, [
              _MenuItem(
                icon: Icons.lock_outline_rounded,
                title: 'Change Password',
                onTap: () => _showChangePasswordSheet(context, ref),
              ),
              _MenuItem(
                icon: Icons.notifications_outlined,
                title: 'Notifications',
                onTap: () {},
              ),
              _MenuItem(
                icon: Icons.security_outlined,
                title: 'Security',
                onTap: () {},
              ),
            ]),

            const SizedBox(height: 16),

            _buildSection(context, [
              _MenuItem(
                icon: Icons.help_outline_rounded,
                title: 'Help & Support',
                onTap: () {},
              ),
              _MenuItem(
                icon: Icons.info_outline_rounded,
                title: 'About MiniBank',
                onTap: () {},
              ),
            ]),

            const SizedBox(height: 24),

            // Logout Button
            AppButton(
              text: 'Sign Out',
              variant: AppButtonVariant.outlined,
              icon: Icons.logout_rounded,
              onPressed: () {
                showDialog(
                  context: context,
                  builder: (context) => AlertDialog(
                    title: const Text('Sign Out'),
                    content: const Text('Are you sure you want to sign out?'),
                    actions: [
                      TextButton(
                        onPressed: () => Navigator.pop(context),
                        child: const Text('Cancel'),
                      ),
                      TextButton(
                        onPressed: () {
                          Navigator.pop(context);
                          ref.read(authProvider.notifier).logout();
                          context.go('/login');
                        },
                        child: Text('Sign Out', style: TextStyle(color: AppColors.error)),
                      ),
                    ],
                  ),
                );
              },
            ),
            const SizedBox(height: 16),
            Text(
              'MiniBank v1.0.0',
              style: Theme.of(context).textTheme.bodySmall,
            ),
            const SizedBox(height: 20),
          ],
        ),
      ),
    );
  }

  Widget _buildSection(BuildContext context, List<_MenuItem> items) {
    return Container(
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: AppColors.border),
      ),
      child: Column(
        children: items.asMap().entries.map((entry) {
          final index = entry.key;
          final item = entry.value;
          return Column(
            children: [
              ListTile(
                contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
                leading: Container(
                  width: 42,
                  height: 42,
                  decoration: BoxDecoration(
                    color: AppColors.surfaceVariant,
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: Icon(item.icon, color: AppColors.primary, size: 22),
                ),
                title: Text(
                  item.title,
                  style: Theme.of(context).textTheme.titleMedium,
                ),
                subtitle: item.subtitle != null
                    ? Text(
                        item.subtitle!,
                        style: Theme.of(context).textTheme.bodySmall,
                      )
                    : null,
                trailing: const Icon(
                  Icons.chevron_right_rounded,
                  color: AppColors.textLight,
                ),
                onTap: item.onTap,
              ),
              if (index < items.length - 1)
                const Divider(height: 1, indent: 72),
            ],
          );
        }).toList(),
      ),
    );
  }

  void _showChangePasswordSheet(BuildContext context, WidgetRef ref) {
    final currentPasswordController = TextEditingController();
    final newPasswordController = TextEditingController();
    final formKey = GlobalKey<FormState>();

    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: AppColors.surface,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      builder: (context) {
        return Padding(
          padding: EdgeInsets.only(
            left: 24, right: 24, top: 24,
            bottom: MediaQuery.of(context).viewInsets.bottom + 24,
          ),
          child: Form(
            key: formKey,
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Center(
                  child: Container(
                    width: 40, height: 4,
                    decoration: BoxDecoration(
                      color: AppColors.border,
                      borderRadius: BorderRadius.circular(2),
                    ),
                  ),
                ),
                const SizedBox(height: 24),
                Text('Change Password', style: Theme.of(context).textTheme.headlineMedium),
                const SizedBox(height: 20),
                AppTextField(
                  label: 'Current Password',
                  hint: 'Enter current password',
                  controller: currentPasswordController,
                  prefixIcon: Icons.lock_outlined,
                  obscureText: true,
                  validator: Validators.password,
                ),
                const SizedBox(height: 16),
                AppTextField(
                  label: 'New Password',
                  hint: 'Enter new password',
                  controller: newPasswordController,
                  prefixIcon: Icons.lock_outline_rounded,
                  obscureText: true,
                  validator: Validators.password,
                ),
                const SizedBox(height: 24),
                Consumer(
                  builder: (context, ref, _) {
                    final state = ref.watch(authProvider);
                    return AppButton(
                      text: 'Change Password',
                      isLoading: state.isLoading,
                      onPressed: () async {
                        if (formKey.currentState!.validate()) {
                          final success = await ref.read(authProvider.notifier)
                              .changePassword(
                                currentPasswordController.text,
                                newPasswordController.text,
                              );
                          if (success && context.mounted) {
                            Navigator.pop(context);
                            ScaffoldMessenger.of(context).showSnackBar(
                              const SnackBar(
                                content: Text('Password changed successfully'),
                                backgroundColor: AppColors.success,
                              ),
                            );
                          }
                        }
                      },
                    );
                  },
                ),
              ],
            ),
          ),
        );
      },
    );
  }
}

class _MenuItem {
  final IconData icon;
  final String title;
  final String? subtitle;
  final VoidCallback onTap;

  _MenuItem({
    required this.icon,
    required this.title,
    this.subtitle,
    required this.onTap,
  });
}
