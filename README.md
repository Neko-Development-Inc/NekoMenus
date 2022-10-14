# NekoMenus
 NekoMenus for pure-forge (forge 1.16.5)

#### This mod is required on servers which uses specific Neko mods, for example
 1. NekoShowdown
 2. NekoPokeBuilder
 3. NekoShop
 4. NekoCrates
 5. NekoStarters

Random examples of how to use this:

```Java
try
{
    Menu menu = Menu.builder();
    menu.setHeight(5)
        .setTitle("&aTest title :) &kOWO")
    .addItem(new MenuItem(menu, logger) {
        final Point pos1 = new Point(1, 1);
        final Point pos2 = new Point(2, 1);
        @Override
        public void onSlotClick(String action, int slotId, int dragType, ClickType clickType, PlayerEntity player, Menu menu) {
            player.sendMessage(StringColorUtils.getColoredString("Action: '" + action + "'"), UUID.randomUUID());
        }
        @Override
        public void onSlotClick(ClickAction action, int slotId, int dragType, ClickType clickType, PlayerEntity player, Menu menu) {
            player.sendMessage(StringColorUtils.getColoredString("Action: '" + action.name() + "'"), UUID.randomUUID());
            MenuItem test = menu.getReferencedItem("test");
            logger.info("Test by ref: " + test);
            if (test != null) {
                if (action == ClickAction.RIGHT) {
                    test.performClick(ClickAction.LEFT, player);
                } else if (action == ClickAction.SLOT_1) {
                    test.incrementAmount();
                } else if (action == ClickAction.SLOT_2) {
                    test.decrementAmount();
                } else if (action == ClickAction.SLOT_3) {
                    test.deleteItem();
                } else if (action == ClickAction.SLOT_4) {
                    test.deleteItem(true);

                } else if (action == ClickAction.SLOT_5) {
                    menu.setTitle((ServerPlayerEntity) player, "New title &c1");
                } else if (action == ClickAction.SLOT_6) {
                    menu.setTitle((ServerPlayerEntity) player, "New title &62");

                } else if (action == ClickAction.SLOT_7) {
                    menu.swapItems(this, test);

                } else if (action == ClickAction.SLOT_8) {
                    menu.moveItem(test, pos1.x, pos1.y);
                } else if (action == ClickAction.SLOT_9) {
                    menu.moveItem(test, pos2.x, pos2.y);
                }
            }
        }
    }.setItem("minecraft:apple")
        .setAction(ClickAction.LEFT, "test click")
        .addClickAction(ClickAction.RIGHT)
        .addClickAction(ClickAction.SLOT_1)
        .addClickAction(ClickAction.SLOT_2)
        .addClickAction(ClickAction.SLOT_3)
        .addClickAction(ClickAction.SLOT_4)
        .addClickAction(ClickAction.SLOT_5)
        .addClickAction(ClickAction.SLOT_6)
        .addClickAction(ClickAction.SLOT_7)
        .addClickAction(ClickAction.SLOT_8)
        .addClickAction(ClickAction.SLOT_9)
        .setLore(Arrays.asList(
            "Press &a1&r to +1",
            "Press &a2&r to -1",
            "Press &a3&r to Delete item (Temp)",
            "Press &a4&r to Delete item (&cpermanently&r)",
            "Press &a5&r to Change title (red)",
            "Press &a6&r to Change title (gold)",
            "Press &a7&r to Swap items",
            "Press &a8&r to Move to pos1",
            "Press &a9&r to Move to pos2"
        ))
        .setName("&b&cOwO")
        .setSlot(12) // 4, 2
        .setClickable(true)
    )

    .addItem(MenuItem.builder().addClickActionLambda(ClickAction.LEFT, (_clickAction, _slot, _playerEntity, _menu) ->
            _playerEntity.sendMessage(StringColorUtils.getColoredString("Hehe: '" + _clickAction.name() + "'"), UUID.randomUUID()))
        .setClickable(true).setSlot(1, 1).setItem("pixelmon:marsh_badge").setName("Test hehe, not hehe").setLore("Hello &cworld")
        .setAsReferencedItem("test").setBypassMaxAmount(true)
    );

    AtomicInteger index = new AtomicInteger();
    menu.runTaskTimer(() -> {
            logger.info("Every 20 ticks :) " + index.getAndIncrement());
            MenuItem test = menu.getReferencedItem("test");
            if (test == null) return;
            test.incrementAmount();
        },
    0, 20);

    AtomicInteger index2 = new AtomicInteger();
    menu.runTaskTimer(() ->
            logger.info("Every 40 ticks :O " + index2.getAndIncrement()),
    0, 40);

    Menu.open(menu, source.asPlayer());
}
catch (Throwable t)
{
    t.printStackTrace();
}
```