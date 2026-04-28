import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../../core/theme/app_colors.dart';
import '../../../../core/utils/formatters.dart';
import '../../../../core/widgets/loading_widget.dart';
import '../../../../core/widgets/error_widget.dart';
import '../../../../core/widgets/app_button.dart';
import '../../../../core/widgets/app_text_field.dart';
import '../../../../core/utils/validators.dart';
import '../../providers/card_provider.dart';
import '../../data/models/card_model.dart';

class CardsScreen extends ConsumerStatefulWidget {
  const CardsScreen({super.key});

  @override
  ConsumerState<CardsScreen> createState() => _CardsScreenState();
}

class _CardsScreenState extends ConsumerState<CardsScreen> {
  int _currentCardIndex = 0;

  @override
  void initState() {
    super.initState();
    _loadCards();
  }

  void _loadCards() {
    const customerId = 'demo-customer-id';
    ref.read(cardProvider.notifier).fetchCards(customerId);
  }

  @override
  Widget build(BuildContext context) {
    final cardState = ref.watch(cardProvider);

    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: AppBar(
        title: const Text('My Cards'),
        actions: [
          IconButton(
            icon: const Icon(Icons.add_card_rounded),
            onPressed: () => _showCreateCardSheet(context),
          ),
        ],
      ),
      body: RefreshIndicator(
        onRefresh: () async => _loadCards(),
        color: AppColors.primary,
        child: cardState.isLoading
            ? const Padding(
                padding: EdgeInsets.all(20),
                child: LoadingWidget(itemCount: 2, height: 200),
              )
            : cardState.error != null
                ? AppErrorWidget(
                    message: cardState.error!,
                    onRetry: _loadCards,
                  )
                : cardState.cards.isEmpty
                    ? EmptyStateWidget(
                        title: 'No Cards',
                        subtitle: 'Apply for a debit or credit card',
                        icon: Icons.credit_card_off_outlined,
                        actionText: 'Apply for Card',
                        onAction: () => _showCreateCardSheet(context),
                      )
                    : SingleChildScrollView(
                        physics: const AlwaysScrollableScrollPhysics(),
                        child: _buildCardContent(context, cardState),
                      ),
      ),
    );
  }

  Widget _buildCardContent(BuildContext context, CardState cardState) {
    final currentCard = cardState.cards.isNotEmpty && _currentCardIndex < cardState.cards.length
        ? cardState.cards[_currentCardIndex]
        : null;

    return Padding(
      padding: const EdgeInsets.all(20),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Visual Card Carousel
          SizedBox(
            height: 210,
            child: PageView.builder(
              itemCount: cardState.cards.length,
              onPageChanged: (index) => setState(() => _currentCardIndex = index),
              itemBuilder: (context, index) {
                return _buildVisualCard(context, cardState.cards[index]);
              },
            ),
          ),

          // Page Indicator
          if (cardState.cards.length > 1)
            Padding(
              padding: const EdgeInsets.only(top: 14),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: List.generate(
                  cardState.cards.length,
                  (index) => AnimatedContainer(
                    duration: const Duration(milliseconds: 200),
                    margin: const EdgeInsets.symmetric(horizontal: 3),
                    width: index == _currentCardIndex ? 24 : 8,
                    height: 8,
                    decoration: BoxDecoration(
                      color: index == _currentCardIndex
                          ? AppColors.primary
                          : AppColors.border,
                      borderRadius: BorderRadius.circular(4),
                    ),
                  ),
                ),
              ),
            ),

          const SizedBox(height: 28),

          // Card Actions
          if (currentCard != null) ...[
            Text('Card Actions', style: Theme.of(context).textTheme.headlineSmall),
            const SizedBox(height: 14),
            _buildCardActions(context, currentCard),
            const SizedBox(height: 28),

            // Card Details
            Text('Card Details', style: Theme.of(context).textTheme.headlineSmall),
            const SizedBox(height: 14),
            _buildCardDetails(context, currentCard),
          ],
        ],
      ),
    );
  }

  Widget _buildVisualCard(BuildContext context, CardModel card) {
    return Container(
      margin: const EdgeInsets.symmetric(horizontal: 4),
      padding: const EdgeInsets.all(24),
      decoration: BoxDecoration(
        color: card.isDebit ? AppColors.primary : AppColors.secondary,
        borderRadius: BorderRadius.circular(20),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          // Top Row - Type & Status
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                card.isDebit ? 'DEBIT CARD' : 'CREDIT CARD',
                style: Theme.of(context).textTheme.labelMedium?.copyWith(
                      color: AppColors.textOnPrimary.withValues(alpha: 0.7),
                      letterSpacing: 2,
                    ),
              ),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                decoration: BoxDecoration(
                  color: card.isActive
                      ? AppColors.success.withValues(alpha: 0.2)
                      : AppColors.error.withValues(alpha: 0.2),
                  borderRadius: BorderRadius.circular(6),
                ),
                child: Text(
                  Formatters.capitalize(card.cardStatus),
                  style: Theme.of(context).textTheme.labelSmall?.copyWith(
                        color: card.isActive ? AppColors.success : AppColors.error,
                        fontWeight: FontWeight.w600,
                      ),
                ),
              ),
            ],
          ),

          // Card Number
          Text(
            Formatters.maskCardNumber(card.cardNumber),
            style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                  color: AppColors.textOnPrimary,
                  letterSpacing: 3,
                  fontWeight: FontWeight.w500,
                ),
          ),

          // Bottom Row - Name & Expiry
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'CARD HOLDER',
                    style: Theme.of(context).textTheme.labelSmall?.copyWith(
                          color: AppColors.textOnPrimary.withValues(alpha: 0.5),
                          fontSize: 9,
                          letterSpacing: 1,
                        ),
                  ),
                  const SizedBox(height: 2),
                  Text(
                    card.cardHolderName.toUpperCase(),
                    style: Theme.of(context).textTheme.titleMedium?.copyWith(
                          color: AppColors.textOnPrimary,
                          letterSpacing: 1,
                        ),
                  ),
                ],
              ),
              Column(
                crossAxisAlignment: CrossAxisAlignment.end,
                children: [
                  Text(
                    'EXPIRES',
                    style: Theme.of(context).textTheme.labelSmall?.copyWith(
                          color: AppColors.textOnPrimary.withValues(alpha: 0.5),
                          fontSize: 9,
                          letterSpacing: 1,
                        ),
                  ),
                  const SizedBox(height: 2),
                  Text(
                    card.expiryDate ?? 'N/A',
                    style: Theme.of(context).textTheme.titleMedium?.copyWith(
                          color: AppColors.textOnPrimary,
                        ),
                  ),
                ],
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildCardActions(BuildContext context, CardModel card) {
    return Row(
      children: [
        Expanded(
          child: _buildActionTile(
            context,
            card.isActive ? Icons.block : Icons.check_circle_outline,
            card.isActive ? 'Block Card' : 'Activate',
            card.isActive ? AppColors.error : AppColors.success,
            () {
              if (card.isActive) {
                _confirmBlock(context, card);
              } else {
                ref.read(cardProvider.notifier).activateCard(card.id);
              }
            },
          ),
        ),
        const SizedBox(width: 12),
        Expanded(
          child: _buildActionTile(
            context,
            Icons.pin_outlined,
            'Change PIN',
            AppColors.accent,
            () => _showChangePinSheet(context, card),
          ),
        ),
      ],
    );
  }

  Widget _buildActionTile(
    BuildContext context,
    IconData icon,
    String label,
    Color color,
    VoidCallback onTap,
  ) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.symmetric(vertical: 16),
        decoration: BoxDecoration(
          color: color.withValues(alpha: 0.08),
          borderRadius: BorderRadius.circular(14),
          border: Border.all(color: color.withValues(alpha: 0.2)),
        ),
        child: Column(
          children: [
            Icon(icon, color: color, size: 28),
            const SizedBox(height: 8),
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

  Widget _buildCardDetails(BuildContext context, CardModel card) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: AppColors.border),
      ),
      child: Column(
        children: [
          _detailRow(context, 'Card Type', card.isDebit ? 'Debit' : 'Credit'),
          const Divider(height: 20),
          _detailRow(context, 'Card Number', Formatters.maskCardNumber(card.cardNumber)),
          const Divider(height: 20),
          _detailRow(context, 'Status', Formatters.capitalize(card.cardStatus)),
          if (card.cardCategory != null) ...[
            const Divider(height: 20),
            _detailRow(context, 'Category', Formatters.capitalize(card.cardCategory!)),
          ],
          if (card.expiryDate != null) ...[
            const Divider(height: 20),
            _detailRow(context, 'Expiry', card.expiryDate!),
          ],
        ],
      ),
    );
  }

  Widget _detailRow(BuildContext context, String label, String value) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Text(label, style: Theme.of(context).textTheme.bodyMedium),
        Text(value, style: Theme.of(context).textTheme.titleMedium),
      ],
    );
  }

  void _confirmBlock(BuildContext context, CardModel card) {
    showDialog(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Block Card'),
        content: const Text('Are you sure you want to block this card? You can reactivate it later.'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Cancel'),
          ),
          TextButton(
            onPressed: () {
              Navigator.pop(context);
              ref.read(cardProvider.notifier).blockCard(card.id);
            },
            child: Text('Block', style: TextStyle(color: AppColors.error)),
          ),
        ],
      ),
    );
  }

  void _showChangePinSheet(BuildContext context, CardModel card) {
    final pinController = TextEditingController();
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
                Text('Change PIN', style: Theme.of(context).textTheme.headlineMedium),
                const SizedBox(height: 20),
                AppTextField(
                  label: 'New PIN',
                  hint: 'Enter 4-digit PIN',
                  controller: pinController,
                  prefixIcon: Icons.pin_outlined,
                  keyboardType: TextInputType.number,
                  obscureText: true,
                  validator: Validators.pin,
                ),
                const SizedBox(height: 24),
                Consumer(
                  builder: (context, ref, _) {
                    final state = ref.watch(cardProvider);
                    return AppButton(
                      text: 'Update PIN',
                      isLoading: state.isOperating,
                      onPressed: () async {
                        if (formKey.currentState!.validate()) {
                          final success = await ref.read(cardProvider.notifier)
                              .updatePin(card.id, pinController.text);
                          if (success && context.mounted) {
                            Navigator.pop(context);
                            ScaffoldMessenger.of(context).showSnackBar(
                              const SnackBar(
                                content: Text('PIN updated successfully'),
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

  void _showCreateCardSheet(BuildContext context) {
    String selectedType = 'DEBIT';
    final pinController = TextEditingController();
    final formKey = GlobalKey<FormState>();

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
                    Text('Apply for Card', style: Theme.of(context).textTheme.headlineMedium),
                    const SizedBox(height: 20),
                    Text('Card Type', style: Theme.of(context).textTheme.labelLarge),
                    const SizedBox(height: 12),
                    Row(
                      children: ['DEBIT', 'CREDIT'].map((type) {
                        final isSelected = selectedType == type;
                        return Expanded(
                          child: GestureDetector(
                            onTap: () => setState(() => selectedType = type),
                            child: Container(
                              margin: EdgeInsets.only(right: type == 'DEBIT' ? 6 : 0, left: type == 'CREDIT' ? 6 : 0),
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
                    const SizedBox(height: 16),
                    AppTextField(
                      label: 'Set PIN',
                      hint: 'Enter 4-digit PIN',
                      controller: pinController,
                      prefixIcon: Icons.pin_outlined,
                      keyboardType: TextInputType.number,
                      obscureText: true,
                      validator: Validators.pin,
                    ),
                    const SizedBox(height: 24),
                    Consumer(
                      builder: (context, ref, _) {
                        final state = ref.watch(cardProvider);
                        return AppButton(
                          text: 'Apply',
                          isLoading: state.isOperating,
                          onPressed: () async {
                            if (formKey.currentState!.validate()) {
                              final success = await ref.read(cardProvider.notifier).createCard(
                                    accountId: 'demo-account-id',
                                    customerId: 'demo-customer-id',
                                    cardType: selectedType,
                                    pin: pinController.text,
                                  );
                              if (success && context.mounted) {
                                Navigator.pop(context);
                                ScaffoldMessenger.of(context).showSnackBar(
                                  const SnackBar(
                                    content: Text('Card applied successfully!'),
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
      },
    );
  }
}
