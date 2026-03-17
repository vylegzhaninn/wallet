- `TransferService.transfer`: Уязвимость (BOLA/IDOR) и логическая ошибка. Нет проверки, что счет списания (`from`) принадлежит инициатору (`userIdFrom`). Любой может списать чужие деньги. Также `userIdTo` берется из DTO, а не из реального счета-получателя, что нарушает целостность истории.
  Решение: 
  ```java
  if (!from.getUserId().equals(request.userIdFrom())) throw new AccessDeniedException("Forbidden");
  Transfer transfer = Transfer.builder().userIdTo(to.getUserId())... // и так далее
  ```

- `UserController.getAllUsers`, `AccountController.getAllAccounts` и `TransferService.getHistory`: Проблема с производительностью (Big O - O(N)) и риск утечки памяти (OOM) из-за отсутствия пагинации при выборке коллекций.
  Решение: Использовать `Pageable` из Spring Data для лимитирования выборок.
  ```java
  public Page<User> getAll(Pageable pageable) { return userRepository.findAll(pageable); }
  ```

- `TransferDto.amount`: Небезопасная проверка суммы. Значение `@DecimalMin("0.0")` с `inclusive=true` разрешает переводы с нулевым балансом, засоряя историю бессмысленными транзакциями.
  Решение: 
  ```java
  @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be strictly greater than zero")
  ```
