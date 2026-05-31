# Aureon

Aureon is a Hypixel SkyBlock dungeon and quality-of-life client mod for modern Fabric. It focuses on dungeon routing, map information, split tracking, cleaner HUDs, and small convenience tools for repeated SkyBlock play.

## Commands

- `/aureon` opens the config menu.
- `/aureon hud` opens the HUD editor.
- `/aureon buttons` opens the inventory button editor.
- `/aureon route start|stop|save|reload|custom|missing` manages secret route recording.
- `/aureon ready` runs a dungeon ready check.
- `/aureon updates status` shows the auto updater status.
- `/aureon updates on` enables update reminders and checks GitHub releases.
- `/aureon updates off` disables update reminders.
- `/aureon summary copy` copies the last run recap if you missed the clickable chat button.
- `/aureon dumpscore` prints the current dungeon score breakdown.
- `/aureon cata <name>` shows Catacombs profile info for a player.

## Dungeon Features

- **Dungeon Splits HUD**: Tracks run time, blood open, watcher clear, boss entry, 270 score, and 300 score.
- **Run PB Tracker**: Saves best split times per floor and shows whether the current run is ahead or behind PB.
- **Blood Camp Timer**: Shows time spent after blood opens and warns when it passes the configured delay.
- **Run Recap**: Prints score, secrets, crypts, deaths, rooms, keys, Mimic, Prince, and split times after a run.
- **Clickable Summary Copy**: Adds a clickable `[COPY SUMMARY]` chat button after the recap.
- **Death Recap**: Labels deaths as `During Clear` or `Boss`.
- **Key Alerts**: Shows local chat alerts when dungeon keys are picked up.
- **Mimic/Prince Status**: Displays Mimic and Prince status in the splits HUD and recap.
- **Dungeon Ready Check**: Reports player count, class duplicates, unknown classes, and team composition.
- **Room Name HUD**: Shows the current dungeon room name, with optional chroma.
- **Score Alerts**: Alerts for 270 score, 300 score, 5 crypts, and bat deaths, with optional party chat messages.
- **Crypt Reminder**: Reminds you to finish crypts after a configurable delay.
- **Dungeon QoL**: Optional dungeon extras including auto requeue, extra stats, Mimic/Prince party announces, and secret sounds.
- **Leap Announce**: Sends a custom party message when you leap to another player.
- **Teammate Missing Alert**: Warns when fewer than five players are detected in a dungeon.
- **Join Info**: Shows Catacombs stats, floor times, armor, and key items when a player joins through Party Finder.
- **Terminal Numbers**: Shows terminal numbers and class labels for F7/M7 terminal assignments.
- **Terminal Tracker**: Tracks terminals, devices, and levers per player, then prints a phase recap with total time and last finisher.

## AureonNav

- **Dungeon Map**: Custom dungeon map with configurable display, behavior, player icons, room colors, and door colors.
- **Boss Map**: Optional map mode for boss.
- **Score Map**: Optional score-focused map view.
- **Separate Map Info**: Renders dungeon map info separately from the main map.
- **Box Wither Doors**: Draws boxes around Wither doors and colors them by key status.
- **Clear Breakdown**: Sends cleared room counts, secrets, and deaths after a run.

## Secrets And Routes

- **Secret Waypoints**: Renders secret waypoints with configurable text scale and colors for chest, item, bat, lever, wither, redstone key, and other waypoint types.
- **Secret Routes**: Loads route files and renders route waypoints for dungeon rooms.
- **Route Controls**: Keybinds for next step, previous step, and custom waypoint creation.
- **Route Rendering Options**: Configurable text, start ESP, text scale, and colors for route actions like mine, superboom, etherwarp, pearl, chest, essence, bat, and lever.
- **Route Recorder**: Records, saves, reloads, and checks missing secret routes.

## Misc Quality Of Life

- **Block Overlay**: Highlights the block you are looking at, with configurable outline, fill, colors, and line width.
- **Inventory Buttons**: Adds custom inventory buttons with an editor, inventory-only mode, and terminal hiding.
- **Pet Display**: Shows active pet information.
- **AutoPet Titles**: Announces AutoPet activations.
- **Health And Mana Bars**: Adds custom health, absorption, mana, and overflow mana bars with optional numeric HUDs.
- **Soulflow Display**: Shows internalized soulflow from soulflow items.
- **Sword Blocking**: Restores 1.8.9-style sword blocking visuals.
- **Cosmetics**: Toggles Aureon cosmetics.
- **HUD Editor**: Lets you move and scale HUD elements.
- **Auto Updater**: Enabled by default. Checks GitHub releases, asks in chat before downloading the newest Aureon jar, and stages accepted updates for install on restart. `/aureon updates off` disables these reminders.

## License

Aureon includes code and UI concepts with third-party attribution listed in `NOTICE` and `THIRD_PARTY_LICENSES/`. See those files for details.
