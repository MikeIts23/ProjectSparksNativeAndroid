cmake_minimum_required(VERSION 3.22.1)
project(Unity)

# Aggiungi il riferimento al prefab game-activity
find_package(game-activity REQUIRED CONFIG)

# Aggiungi la sottocartella GameActivity generata da Unity
add_subdirectory(GameActivity)

# Collega esplicitamente game-activity
target_link_libraries(
        Unity
        game-activity::game-activity
)
