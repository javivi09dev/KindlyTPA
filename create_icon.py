import os
from PIL import Image, ImageDraw

# Set dimensions
width, height = 512, 512

# Create new image with white background
image = Image.new("RGBA", (width, height), (0, 0, 0, 0))
draw = ImageDraw.Draw(image)

# Define colors
background_color = (41, 128, 185)  # Dark blue
teleport_color = (236, 240, 241)   # White/light gray
shield_color = (46, 204, 113)      # Green
border_color = (255, 255, 255)     # White

# Center point
center = (width // 2, height // 2)

# Draw rounded background
def rounded_rectangle(draw, rect, radius, color):
    x1, y1, x2, y2 = rect
    draw.rectangle((x1 + radius, y1, x2 - radius, y2), fill=color)
    draw.rectangle((x1, y1 + radius, x2, y2 - radius), fill=color)
    draw.ellipse((x1, y1, x1 + 2 * radius, y1 + 2 * radius), fill=color)
    draw.ellipse((x2 - 2 * radius, y1, x2, y1 + 2 * radius), fill=color)
    draw.ellipse((x1, y2 - 2 * radius, x1 + 2 * radius, y2), fill=color)
    draw.ellipse((x2 - 2 * radius, y2 - 2 * radius, x2, y2), fill=color)

# Draw main background
rounded_rectangle(draw, (40, 40, width - 40, height - 40), 60, background_color)

# Draw large "TP" text-like symbol (simplified)
# Draw the "T"
t_width = 120
t_height = 160
t_top = 120
draw.rectangle((center[0] - t_width/2, t_top, center[0] + t_width/2, t_top + 30), fill=teleport_color)  # T top
draw.rectangle((center[0] - 25, t_top, center[0] + 25, t_top + t_height), fill=teleport_color)  # T stem

# Draw the "P"
p_left = center[0] + 50
p_top = t_top + 20
p_width = 70
p_height = 90
draw.rectangle((p_left, p_top, p_left + 30, p_top + 140), fill=teleport_color)  # P stem
draw.rectangle((p_left, p_top, p_left + p_width, p_top + 30), fill=teleport_color)  # P top
draw.rectangle((p_left, p_top + p_height - 30, p_left + p_width, p_top + p_height), fill=teleport_color)  # P bottom
draw.rectangle((p_left + p_width - 30, p_top, p_left + p_width, p_top + p_height), fill=teleport_color)  # P right

# Draw shield in bottom half
shield_width = 180
shield_height = 220
shield_top = 290
shield_bottom = shield_top + shield_height

# Shield background (simplified shield shape)
shield_points = [
    (center[0], shield_top),                         # Top point
    (center[0] + shield_width/2, shield_top + 40),   # Top right
    (center[0] + shield_width/3, shield_bottom),     # Bottom right
    (center[0], shield_bottom - 20),                 # Bottom middle
    (center[0] - shield_width/3, shield_bottom),     # Bottom left
    (center[0] - shield_width/2, shield_top + 40),   # Top left
]
draw.polygon(shield_points, fill=shield_color)

# Add white border to shield
draw.line(shield_points + [shield_points[0]], fill=border_color, width=6)

# Draw a simple check mark in the shield
checkmark_points = [
    (center[0] - 40, shield_top + 100),
    (center[0], shield_top + 140),
    (center[0] + 60, shield_top + 60)
]
draw.line(checkmark_points, fill=border_color, width=12)

# Save the image
output_path = "e:/ModdingMinecraft/KindlyTPA/kindlytpa_icon.png"
image.save(output_path)
print(f"Icon saved to {output_path}")

# Open the image
try:
    os.startfile(output_path)
except:
    pass  # Skip if not on Windows