import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/utils/formatters.dart';
import '../../../../core/widgets/loading_widget.dart';
import '../../../../core/widgets/error_widget.dart';
import '../../providers/transaction_provider.dart';
import '../../data/models/transaction_model.dart';

class TransactionsScreen extends ConsumerStatefulWidget {
  const TransactionsScreen({super.key});

  @override
  ConsumerState<TransactionsScreen> createState() => _TransactionsScreenState();
}

class _TransactionsScreenState extends ConsumerState<TransactionsScreen> {
  String _selectedFilter = 'All';

  @override
  void initState() {
    super.initState();
    _loadTransactions();
  }

  void _loadTransactions() {
    const customerId = 'demo-customer-id';
    ref.read(transactionProvider.notifier).fetchTransactionsByCustomer(customerId);
  }

  @override
  Widget build(BuildContext context) {
    final txnState = ref.watch(transactionProvider);

    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: AppBar(
        title: const Text('Transactions'),
      ),
      body: Column(
        children: [
          // Filter Chips
          SingleChildScrollView(
            scrollDirection: Axis.horizontal,
            padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 8),
            child: Row(
              children: ['All', 'Credit', 'Debit', 'Success', 'Pending', 'Failed']
                  .map((filter) => _buildFilterChip(context, filter))
                  .toList(),
            ),
          ),

          // Transaction List
          Expanded(
            child: RefreshIndicator(
              onRefresh: () async => _loadTransactions(),
              color: AppColors.primary,
              child: txnState.isLoading
                  ? const Padding(
                      padding: EdgeInsets.all(20),
                      child: LoadingWidget(itemCount: 6, height: 70),
                    )
                  : txnState.error != null
                      ? AppErrorWidget(
                          message: txnState.error!,
                          onRetry: _loadTransactions,
                        )
                      : _buildTransactionList(context, txnState),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildFilterChip(BuildContext context, String filter) {
    final isSelected = _selectedFilter == filter;
    return Padding(
      padding: const EdgeInsets.only(right: 8),
      child: GestureDetector(
        onTap: () => setState(() => _selectedFilter = filter),
        child: AnimatedContainer(
          duration: const Duration(milliseconds: 200),
          padding: const EdgeInsets.symmetric(horizontal: 18, vertical: 10),
          decoration: BoxDecoration(
            color: isSelected ? AppColors.primary : AppColors.surface,
            borderRadius: BorderRadius.circular(24),
            border: Border.all(
              color: isSelected ? AppColors.primary : AppColors.border,
            ),
          ),
          child: Text(
            filter,
            style: Theme.of(context).textTheme.labelMedium?.copyWith(
                  color: isSelected ? AppColors.textOnPrimary : AppColors.textSecondary,
                  fontWeight: isSelected ? FontWeight.w600 : FontWeight.w400,
                ),
          ),
        ),
      ),
    );
  }

  Widget _buildTransactionList(BuildContext context, TransactionState txnState) {
    var filtered = txnState.transactions;

    if (_selectedFilter != 'All') {
      filtered = filtered.where((txn) {
        switch (_selectedFilter) {
          case 'Credit':
            return txn.isCredit;
          case 'Debit':
            return txn.isDebit;
          case 'Success':
            return txn.isSuccess;
          case 'Pending':
            return txn.isPending;
          case 'Failed':
            return txn.isFailed;
          default:
            return true;
        }
      }).toList();
    }

    if (filtered.isEmpty) {
      return EmptyStateWidget(
        title: 'No Transactions',
        subtitle: _selectedFilter == 'All'
            ? 'Your transactions will appear here'
            : 'No $_selectedFilter transactions found',
        icon: Icons.receipt_long_outlined,
      );
    }

    // Group by date
    final grouped = <String, List<TransactionModel>>{};
    for (final txn in filtered) {
      final dateKey = txn.transactionDate != null
          ? Formatters.date(txn.transactionDate!)
          : 'Unknown Date';
      grouped.putIfAbsent(dateKey, () => []).add(txn);
    }

    return ListView.builder(
      padding: const EdgeInsets.symmetric(horizontal: 20),
      itemCount: grouped.length,
      itemBuilder: (context, index) {
        final dateKey = grouped.keys.elementAt(index);
        final transactions = grouped[dateKey]!;

        return Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Padding(
              padding: const EdgeInsets.symmetric(vertical: 12),
              child: Text(
                dateKey,
                style: Theme.of(context).textTheme.labelMedium?.copyWith(
                      color: AppColors.textLight,
                    ),
              ),
            ),
            ...transactions.map((txn) => _buildTransactionTile(context, txn)),
          ],
        );
      },
    );
  }

  Widget _buildTransactionTile(BuildContext context, TransactionModel txn) {
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
                const SizedBox(height: 4),
                Row(
                  children: [
                    _buildStatusBadge(context, txn),
                    const SizedBox(width: 8),
                    if (txn.referenceId != null)
                      Text(
                        '#${Formatters.shortUuid(txn.referenceId!)}',
                        style: Theme.of(context).textTheme.bodySmall,
                      ),
                  ],
                ),
              ],
            ),
          ),
          Column(
            crossAxisAlignment: CrossAxisAlignment.end,
            children: [
              Text(
                '${txn.isCredit ? '+' : '-'}${Formatters.currency(txn.amount)}',
                style: Theme.of(context).textTheme.titleMedium?.copyWith(
                      color: txn.isCredit ? AppColors.credit : AppColors.debit,
                      fontWeight: FontWeight.w600,
                    ),
              ),
              if (txn.transactionDate != null) ...[
                const SizedBox(height: 4),
                Text(
                  Formatters.relativeDate(txn.transactionDate!),
                  style: Theme.of(context).textTheme.bodySmall,
                ),
              ],
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildStatusBadge(BuildContext context, TransactionModel txn) {
    Color color;
    if (txn.isSuccess) {
      color = AppColors.success;
    } else if (txn.isPending) {
      color = AppColors.warning;
    } else {
      color = AppColors.error;
    }

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
      decoration: BoxDecoration(
        color: color.withValues(alpha: 0.1),
        borderRadius: BorderRadius.circular(6),
      ),
      child: Text(
        Formatters.capitalize(txn.transactionStatus),
        style: Theme.of(context).textTheme.labelSmall?.copyWith(
              color: color,
              fontWeight: FontWeight.w600,
              fontSize: 10,
            ),
      ),
    );
  }
}
