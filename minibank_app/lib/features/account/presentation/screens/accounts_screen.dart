import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/utils/formatters.dart';
import '../../../../core/widgets/loading_widget.dart';
import '../../../../core/widgets/error_widget.dart';
import '../../../../core/widgets/app_button.dart';
import '../../../../core/widgets/app_text_field.dart';
import '../../../../core/utils/validators.dart';
import '../../providers/account_provider.dart';
import '../../data/models/account_model.dart';

class AccountsScreen extends ConsumerStatefulWidget {
  const AccountsScreen({super.key});

  @override
  ConsumerState<AccountsScreen> createState() => _AccountsScreenState();
}

class _AccountsScreenState extends ConsumerState<AccountsScreen> {
  @override
  void initState() {
    super.initState();
    _loadAccounts();
  }

  void _loadAccounts() {
    const customerId = 'demo-customer-id';
    ref.read(accountProvider.notifier).fetchAccounts(customerId);
  }

  @override
  Widget build(BuildContext context) {
    final accountState = ref.watch(accountProvider);

    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: AppBar(
        title: const Text('My Accounts'),
        actions: [
          IconButton(
            icon: const Icon(Icons.add_circle_outline),
            onPressed: () => _showCreateAccountSheet(context),
          ),
        ],
      ),
      body: RefreshIndicator(
        onRefresh: () async => _loadAccounts(),
        color: AppColors.primary,
        child: accountState.isLoading
            ? const Padding(
                padding: EdgeInsets.all(20),
                child: LoadingWidget(itemCount: 4),
              )
            : accountState.error != null
                ? AppErrorWidget(
                    message: accountState.error!,
                    onRetry: _loadAccounts,
                  )
                : accountState.accounts.isEmpty
                    ? EmptyStateWidget(
                        title: 'No Accounts',
                        subtitle: 'Create your first bank account to get started',
                        icon: Icons.account_balance_outlined,
                        actionText: 'Create Account',
                        onAction: () => _showCreateAccountSheet(context),
                      )
                    : ListView.builder(
                        padding: const EdgeInsets.all(20),
                        itemCount: accountState.accounts.length,
                        itemBuilder: (context, index) {
                          return _buildAccountCard(
                            context,
                            accountState.accounts[index],
                          );
                        },
                      ),
      ),
    );
  }

  Widget _buildAccountCard(BuildContext context, AccountModel account) {
    return GestureDetector(
      onTap: () => _showAccountDetail(context, account),
      child: Container(
        margin: const EdgeInsets.only(bottom: 14),
        padding: const EdgeInsets.all(20),
        decoration: BoxDecoration(
          color: AppColors.surface,
          borderRadius: BorderRadius.circular(16),
          border: Border.all(color: AppColors.border),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Container(
                  width: 50,
                  height: 50,
                  decoration: BoxDecoration(
                    color: account.accountType == 'SAVINGS'
                        ? AppColors.success.withValues(alpha: 0.1)
                        : AppColors.accent.withValues(alpha: 0.1),
                    borderRadius: BorderRadius.circular(14),
                  ),
                  child: Icon(
                    account.accountType == 'SAVINGS'
                        ? Icons.savings_outlined
                        : Icons.account_balance_outlined,
                    color: account.accountType == 'SAVINGS'
                        ? AppColors.success
                        : AppColors.accent,
                    size: 26,
                  ),
                ),
                const SizedBox(width: 14),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        '${Formatters.capitalize(account.accountType)} Account',
                        style: Theme.of(context).textTheme.titleLarge,
                      ),
                      const SizedBox(height: 2),
                      Text(
                        Formatters.maskAccountNumber(account.accountNumber),
                        style: Theme.of(context).textTheme.bodySmall,
                      ),
                    ],
                  ),
                ),
                _buildStatusChip(context, account),
              ],
            ),
            const SizedBox(height: 18),
            const Divider(),
            const SizedBox(height: 14),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Available Balance',
                      style: Theme.of(context).textTheme.bodySmall,
                    ),
                    const SizedBox(height: 4),
                    Text(
                      Formatters.currency(account.accountBalance),
                      style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                            fontWeight: FontWeight.w700,
                          ),
                    ),
                  ],
                ),
                Row(
                  children: [
                    _buildActionButton(
                      context,
                      Icons.add_rounded,
                      'Deposit',
                      AppColors.success,
                      () => _showDepositSheet(context, account),
                    ),
                    const SizedBox(width: 10),
                    _buildActionButton(
                      context,
                      Icons.remove_rounded,
                      'Withdraw',
                      AppColors.warning,
                      () => _showWithdrawSheet(context, account),
                    ),
                  ],
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildStatusChip(BuildContext context, AccountModel account) {
    final isActive = account.isActive;
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
      decoration: BoxDecoration(
        color: (isActive ? AppColors.success : AppColors.error).withValues(alpha: 0.1),
        borderRadius: BorderRadius.circular(20),
      ),
      child: Text(
        isActive ? 'Active' : 'Blocked',
        style: Theme.of(context).textTheme.labelSmall?.copyWith(
              color: isActive ? AppColors.success : AppColors.error,
              fontWeight: FontWeight.w600,
            ),
      ),
    );
  }

  Widget _buildActionButton(
    BuildContext context,
    IconData icon,
    String label,
    Color color,
    VoidCallback onTap,
  ) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
        decoration: BoxDecoration(
          color: color.withValues(alpha: 0.1),
          borderRadius: BorderRadius.circular(10),
        ),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(icon, size: 18, color: color),
            const SizedBox(width: 4),
            Text(
              label,
              style: Theme.of(context).textTheme.labelMedium?.copyWith(
                    color: color,
                    fontWeight: FontWeight.w600,
                  ),
            ),
          ],
        ),
      ),
    );
  }

  void _showAccountDetail(BuildContext context, AccountModel account) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: AppColors.surface,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      builder: (context) {
        return Padding(
          padding: const EdgeInsets.all(24),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Center(
                child: Container(
                  width: 40,
                  height: 4,
                  decoration: BoxDecoration(
                    color: AppColors.border,
                    borderRadius: BorderRadius.circular(2),
                  ),
                ),
              ),
              const SizedBox(height: 24),
              Text('Account Details', style: Theme.of(context).textTheme.headlineMedium),
              const SizedBox(height: 20),
              _detailRow(context, 'Account Number', account.accountNumber),
              _detailRow(context, 'Account Type', Formatters.capitalize(account.accountType)),
              _detailRow(context, 'Status', Formatters.capitalize(account.accountStatus)),
              _detailRow(context, 'Balance', Formatters.currency(account.accountBalance)),
              if (account.createdAt != null)
                _detailRow(context, 'Opened', Formatters.date(account.createdAt!)),
              const SizedBox(height: 20),
            ],
          ),
        );
      },
    );
  }

  Widget _detailRow(BuildContext context, String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 10),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(label, style: Theme.of(context).textTheme.bodyMedium),
          Text(value, style: Theme.of(context).textTheme.titleMedium),
        ],
      ),
    );
  }

  void _showDepositSheet(BuildContext context, AccountModel account) {
    _showAmountSheet(context, 'Deposit', AppColors.success, (amount) async {
      final success = await ref.read(accountProvider.notifier).deposit(account.id, amount);
      if (success && mounted) {
        Navigator.pop(context);
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Deposited ${Formatters.currency(amount)} successfully'),
            backgroundColor: AppColors.success,
          ),
        );
      }
    });
  }

  void _showWithdrawSheet(BuildContext context, AccountModel account) {
    _showAmountSheet(context, 'Withdraw', AppColors.warning, (amount) async {
      final success = await ref.read(accountProvider.notifier).withdraw(account.id, amount);
      if (success && mounted) {
        Navigator.pop(context);
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Withdrawn ${Formatters.currency(amount)} successfully'),
            backgroundColor: AppColors.success,
          ),
        );
      }
    });
  }

  void _showAmountSheet(
    BuildContext context,
    String title,
    Color color,
    Future<void> Function(double) onSubmit,
  ) {
    final amountController = TextEditingController();
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
            left: 24,
            right: 24,
            top: 24,
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
                    width: 40,
                    height: 4,
                    decoration: BoxDecoration(
                      color: AppColors.border,
                      borderRadius: BorderRadius.circular(2),
                    ),
                  ),
                ),
                const SizedBox(height: 24),
                Text(title, style: Theme.of(context).textTheme.headlineMedium),
                const SizedBox(height: 20),
                AppTextField(
                  label: 'Amount',
                  hint: 'Enter amount',
                  controller: amountController,
                  prefixIcon: Icons.attach_money_rounded,
                  keyboardType: TextInputType.number,
                  validator: Validators.amount,
                ),
                const SizedBox(height: 24),
                Consumer(
                  builder: (context, ref, _) {
                    final state = ref.watch(accountProvider);
                    return AppButton(
                      text: title,
                      isLoading: state.isOperating,
                      onPressed: () {
                        if (formKey.currentState!.validate()) {
                          onSubmit(double.parse(amountController.text));
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

  void _showCreateAccountSheet(BuildContext context) {
    String selectedType = 'SAVINGS';

    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      backgroundColor: AppColors.surface,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
      ),
      builder: (context) {
        return StatefulBuilder(
          builder: (context, setState) {
            return Padding(
              padding: EdgeInsets.only(
                left: 24,
                right: 24,
                top: 24,
                bottom: MediaQuery.of(context).viewInsets.bottom + 24,
              ),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Center(
                    child: Container(
                      width: 40,
                      height: 4,
                      decoration: BoxDecoration(
                        color: AppColors.border,
                        borderRadius: BorderRadius.circular(2),
                      ),
                    ),
                  ),
                  const SizedBox(height: 24),
                  Text(
                    'Create Account',
                    style: Theme.of(context).textTheme.headlineMedium,
                  ),
                  const SizedBox(height: 20),
                  Text('Account Type', style: Theme.of(context).textTheme.labelLarge),
                  const SizedBox(height: 12),
                  Row(
                    children: ['SAVINGS', 'CURRENT'].map((type) {
                      final isSelected = selectedType == type;
                      return Expanded(
                        child: GestureDetector(
                          onTap: () => setState(() => selectedType = type),
                          child: Container(
                            margin: EdgeInsets.only(right: type == 'SAVINGS' ? 6 : 0, left: type == 'CURRENT' ? 6 : 0),
                            padding: const EdgeInsets.symmetric(vertical: 14),
                            decoration: BoxDecoration(
                              color: isSelected ? AppColors.primary : AppColors.surfaceVariant,
                              borderRadius: BorderRadius.circular(12),
                            ),
                            alignment: Alignment.center,
                            child: Text(
                              Formatters.capitalize(type),
                              style: Theme.of(context).textTheme.titleMedium?.copyWith(
                                    color: isSelected ? AppColors.textOnPrimary : AppColors.textPrimary,
                                  ),
                            ),
                          ),
                        ),
                      );
                    }).toList(),
                  ),
                  const SizedBox(height: 24),
                  Consumer(
                    builder: (context, ref, _) {
                      final state = ref.watch(accountProvider);
                      return AppButton(
                        text: 'Create Account',
                        isLoading: state.isOperating,
                        onPressed: () async {
                          const customerId = 'demo-customer-id';
                          final success = await ref
                              .read(accountProvider.notifier)
                              .createAccount(customerId, selectedType);
                          if (success && context.mounted) {
                            Navigator.pop(context);
                            ScaffoldMessenger.of(context).showSnackBar(
                              const SnackBar(
                                content: Text('Account created successfully!'),
                                backgroundColor: AppColors.success,
                              ),
                            );
                          }
                        },
                      );
                    },
                  ),
                ],
              ),
            );
          },
        );
      },
    );
  }
}
