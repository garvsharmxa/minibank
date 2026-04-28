import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/theme/app_colors.dart';
import '../../../../core/utils/formatters.dart';
import '../../../../core/widgets/loading_widget.dart';
import '../../../../core/widgets/error_widget.dart';
import '../../../account/providers/account_provider.dart';
import '../../../transaction/providers/transaction_provider.dart';

class DashboardScreen extends ConsumerStatefulWidget {
  const DashboardScreen({super.key});

  @override
  ConsumerState<DashboardScreen> createState() => _DashboardScreenState();
}

class _DashboardScreenState extends ConsumerState<DashboardScreen> {
  @override
  void initState() {
    super.initState();
    _loadData();
  }

  void _loadData() {
    // Using a demo customer ID - in production, get from auth state
    const customerId = 'demo-customer-id';
    ref.read(accountProvider.notifier).fetchAccounts(customerId);
    ref.read(transactionProvider.notifier).fetchTransactionsByCustomer(customerId);
  }

  @override
  Widget build(BuildContext context) {
    final accountState = ref.watch(accountProvider);
    final transactionState = ref.watch(transactionProvider);

    return Scaffold(
      backgroundColor: AppColors.background,
      body: SafeArea(
        child: RefreshIndicator(
          onRefresh: () async => _loadData(),
          color: AppColors.primary,
          child: SingleChildScrollView(
            physics: const AlwaysScrollableScrollPhysics(),
            padding: const EdgeInsets.symmetric(horizontal: 20),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const SizedBox(height: 20),

                // Header
                _buildHeader(context),
                const SizedBox(height: 24),

                // Balance Card
                _buildBalanceCard(context, accountState),
                const SizedBox(height: 24),

                // Quick Actions
                _buildQuickActions(context),
                const SizedBox(height: 28),

                // My Accounts
                _buildAccountsSection(context, accountState),
                const SizedBox(height: 28),

                // Recent Transactions
                _buildTransactionsSection(context, transactionState),
                const SizedBox(height: 20),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildHeader(BuildContext context) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              _getGreeting(),
              style: Theme.of(context).textTheme.bodyMedium,
            ),
            const SizedBox(height: 4),
            Text(
              'Welcome back!',
              style: Theme.of(context).textTheme.headlineLarge,
            ),
          ],
        ),
        Container(
          width: 48,
          height: 48,
          decoration: BoxDecoration(
            color: AppColors.primary,
            borderRadius: BorderRadius.circular(14),
          ),
          child: const Icon(
            Icons.notifications_outlined,
            color: AppColors.textOnPrimary,
            size: 24,
          ),
        ),
      ],
    );
  }

  Widget _buildBalanceCard(BuildContext context, AccountState accountState) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(24),
      decoration: BoxDecoration(
        color: AppColors.primary,
        borderRadius: BorderRadius.circular(20),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Total Balance',
            style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                  color: AppColors.textOnPrimary.withValues(alpha: 0.7),
                ),
          ),
          const SizedBox(height: 8),
          accountState.isLoading
              ? const ShimmerCard(width: 200, height: 40)
              : Text(
                  Formatters.currency(accountState.totalBalance),
                  style: Theme.of(context).textTheme.displayLarge?.copyWith(
                        color: AppColors.textOnPrimary,
                        fontSize: 36,
                        fontWeight: FontWeight.w700,
                      ),
                ),
          const SizedBox(height: 16),
          Row(
            children: [
              _buildBalanceChip(
                context,
                '${accountState.accounts.length} Accounts',
                Icons.account_balance_wallet_outlined,
              ),
              const SizedBox(width: 12),
              _buildBalanceChip(
                context,
                'Active',
                Icons.check_circle_outline,
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildBalanceChip(BuildContext context, String label, IconData icon) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
      decoration: BoxDecoration(
        color: AppColors.textOnPrimary.withValues(alpha: 0.15),
        borderRadius: BorderRadius.circular(20),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(icon, size: 16, color: AppColors.textOnPrimary.withValues(alpha: 0.8)),
          const SizedBox(width: 6),
          Text(
            label,
            style: Theme.of(context).textTheme.labelSmall?.copyWith(
                  color: AppColors.textOnPrimary.withValues(alpha: 0.8),
                ),
          ),
        ],
      ),
    );
  }

  Widget _buildQuickActions(BuildContext context) {
    final actions = [
      _QuickAction('Send', Icons.send_rounded, AppColors.accent, () {}),
      _QuickAction('Deposit', Icons.add_circle_outline, AppColors.success, () {}),
      _QuickAction('Withdraw', Icons.remove_circle_outline, AppColors.warning, () {}),
      _QuickAction('Cards', Icons.credit_card_rounded, AppColors.primary, () {
        // Navigate to cards tab via bottom nav
      }),
    ];

    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: actions.map((action) {
        return GestureDetector(
          onTap: action.onTap,
          child: Column(
            children: [
              Container(
                width: 60,
                height: 60,
                decoration: BoxDecoration(
                  color: action.color.withValues(alpha: 0.1),
                  borderRadius: BorderRadius.circular(16),
                ),
                child: Icon(action.icon, color: action.color, size: 28),
              ),
              const SizedBox(height: 8),
              Text(
                action.label,
                style: Theme.of(context).textTheme.labelMedium,
              ),
            ],
          ),
        );
      }).toList(),
    );
  }

  Widget _buildAccountsSection(BuildContext context, AccountState accountState) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text('My Accounts', style: Theme.of(context).textTheme.headlineSmall),
            TextButton(
              onPressed: () {},
              child: const Text('See All'),
            ),
          ],
        ),
        const SizedBox(height: 12),
        if (accountState.isLoading)
          const LoadingWidget(itemCount: 2, height: 80)
        else if (accountState.error != null)
          AppErrorWidget(message: accountState.error!, onRetry: _loadData)
        else if (accountState.accounts.isEmpty)
          Container(
            padding: const EdgeInsets.all(20),
            decoration: BoxDecoration(
              color: AppColors.surface,
              borderRadius: BorderRadius.circular(14),
              border: Border.all(color: AppColors.border),
            ),
            child: Row(
              children: [
                Container(
                  width: 48,
                  height: 48,
                  decoration: BoxDecoration(
                    color: AppColors.surfaceVariant,
                    borderRadius: BorderRadius.circular(12),
                  ),
                  child: const Icon(Icons.add_rounded, color: AppColors.accent),
                ),
                const SizedBox(width: 14),
                Expanded(
                  child: Text(
                    'No accounts yet. Create your first account!',
                    style: Theme.of(context).textTheme.bodyMedium,
                  ),
                ),
              ],
            ),
          )
        else
          ...accountState.accounts.take(2).map((account) {
            return Container(
              margin: const EdgeInsets.only(bottom: 10),
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: AppColors.surface,
                borderRadius: BorderRadius.circular(14),
                border: Border.all(color: AppColors.border),
              ),
              child: Row(
                children: [
                  Container(
                    width: 48,
                    height: 48,
                    decoration: BoxDecoration(
                      color: account.accountType == 'SAVINGS'
                          ? AppColors.success.withValues(alpha: 0.1)
                          : AppColors.accent.withValues(alpha: 0.1),
                      borderRadius: BorderRadius.circular(12),
                    ),
                    child: Icon(
                      account.accountType == 'SAVINGS'
                          ? Icons.savings_outlined
                          : Icons.account_balance_outlined,
                      color: account.accountType == 'SAVINGS'
                          ? AppColors.success
                          : AppColors.accent,
                    ),
                  ),
                  const SizedBox(width: 14),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          '${Formatters.capitalize(account.accountType)} Account',
                          style: Theme.of(context).textTheme.titleMedium,
                        ),
                        const SizedBox(height: 2),
                        Text(
                          Formatters.maskAccountNumber(account.accountNumber),
                          style: Theme.of(context).textTheme.bodySmall,
                        ),
                      ],
                    ),
                  ),
                  Text(
                    Formatters.currency(account.accountBalance),
                    style: Theme.of(context).textTheme.titleLarge,
                  ),
                ],
              ),
            );
          }),
      ],
    );
  }

  Widget _buildTransactionsSection(BuildContext context, TransactionState txnState) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text('Recent Transactions', style: Theme.of(context).textTheme.headlineSmall),
            TextButton(
              onPressed: () {},
              child: const Text('See All'),
            ),
          ],
        ),
        const SizedBox(height: 12),
        if (txnState.isLoading)
          const LoadingWidget(itemCount: 3, height: 70)
        else if (txnState.transactions.isEmpty)
          Container(
            padding: const EdgeInsets.all(32),
            alignment: Alignment.center,
            child: Column(
              children: [
                Icon(Icons.receipt_long_outlined, size: 48, color: AppColors.textLight),
                const SizedBox(height: 12),
                Text(
                  'No transactions yet',
                  style: Theme.of(context).textTheme.bodyMedium,
                ),
              ],
            ),
          )
        else
          ...txnState.recentTransactions.map((txn) {
            return Container(
              margin: const EdgeInsets.only(bottom: 8),
              padding: const EdgeInsets.all(14),
              decoration: BoxDecoration(
                color: AppColors.surface,
                borderRadius: BorderRadius.circular(14),
                border: Border.all(color: AppColors.border),
              ),
              child: Row(
                children: [
                  Container(
                    width: 44,
                    height: 44,
                    decoration: BoxDecoration(
                      color: txn.isCredit
                          ? AppColors.credit.withValues(alpha: 0.1)
                          : AppColors.debit.withValues(alpha: 0.1),
                      borderRadius: BorderRadius.circular(12),
                    ),
                    child: Icon(
                      txn.isCredit
                          ? Icons.arrow_downward_rounded
                          : Icons.arrow_upward_rounded,
                      color: txn.isCredit ? AppColors.credit : AppColors.debit,
                      size: 22,
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          txn.description ?? Formatters.capitalize(txn.transactionType),
                          style: Theme.of(context).textTheme.titleMedium,
                        ),
                        const SizedBox(height: 2),
                        Text(
                          txn.transactionDate != null
                              ? Formatters.relativeDate(txn.transactionDate!)
                              : '',
                          style: Theme.of(context).textTheme.bodySmall,
                        ),
                      ],
                    ),
                  ),
                  Text(
                    '${txn.isCredit ? '+' : '-'}${Formatters.currency(txn.amount)}',
                    style: Theme.of(context).textTheme.titleMedium?.copyWith(
                          color: txn.isCredit ? AppColors.credit : AppColors.debit,
                          fontWeight: FontWeight.w600,
                        ),
                  ),
                ],
              ),
            );
          }),
      ],
    );
  }

  String _getGreeting() {
    final hour = DateTime.now().hour;
    if (hour < 12) return 'Good Morning ☀️';
    if (hour < 17) return 'Good Afternoon 🌤️';
    return 'Good Evening 🌙';
  }
}

class _QuickAction {
  final String label;
  final IconData icon;
  final Color color;
  final VoidCallback onTap;

  _QuickAction(this.label, this.icon, this.color, this.onTap);
}
