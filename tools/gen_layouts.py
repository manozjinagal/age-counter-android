"""Generates one widget layout per style and size: res/layout/w_<style>_<size>.xml,
plus Styles.kt that maps them. Run: python3 tools/gen_layouts.py"""
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
LAYOUT = os.path.join(ROOT, 'app/src/main/res/layout')
KT = os.path.join(ROOT, 'app/src/main/java/com/manoj/agecounter/Styles.kt')

STYLES = {
    # key: name, font, number colour, clock colour, separator kind, glow
    'odometer': dict(name='Rolling odometer', font='sans-serif', bold=True, color='#FFFFFFFF', clock='#FFFFFFFF', sep='colon'),
    'ticker':   dict(name='Mono ticker', font='monospace', bold=False, color='#FFFFFFFF', clock='#FFFB6A40', sep='colon'),
    'stacked':  dict(name='Stacked labels', font='sans-serif-light', bold=False, color='#FFFFFFFF', clock='#FFFFFFFF', sep='line'),
    'hero':     dict(name='Hero years', font='sans-serif-thin', bold=False, color='#FFFFFFFF', clock='#FFFFFFFF', sep='colon'),
    'progress': dict(name='Life progress', font='sans-serif-light', bold=False, color='#FFFFFFFF', clock='#FFFFFFFF', sep='colon'),
    'led':      dict(name='Dot-matrix LED', font='@font/doto', bold=False, color='#FFFFD9A8', clock='#FFFFD9A8', sep='colon', glow='#CCFF9A3C'),
}
SIZES = ['compact', 'small', 'wide', 'large']
A = 'android:'

def num(st, id_, size, extra=''):
    shadow = (f'{A}shadowColor="{st["glow"]}" {A}shadowRadius="10" {A}shadowDx="0" {A}shadowDy="0"' if st.get('glow')
              else f'{A}shadowColor="#73000000" {A}shadowRadius="6" {A}shadowDx="0" {A}shadowDy="1"')
    tag = 'Chronometer' if id_ == 'clock' else 'TextView'
    color = st['clock'] if id_ == 'clock' else st['color']
    idattr = f'{A}id="@+id/{id_}" ' if id_ else ''
    return (f'<{tag} {idattr}{A}layout_width="wrap_content" {A}layout_height="wrap_content" '
            f'{A}textSize="{size}sp" {A}textColor="{color}" {A}fontFamily="{st["font"]}" '
            f'{A}textStyle="{"bold" if st["bold"] else "normal"}" {A}fontFeatureSettings="tnum" '
            f'{A}includeFontPadding="false" {shadow} {extra}/>')

def label(text, size=9, extra='', color='#A6FFFFFF'):
    return (f'<TextView {A}layout_width="wrap_content" {A}layout_height="wrap_content" {A}text="{text}" '
            f'{A}textSize="{size}sp" {A}textColor="{color}" {A}fontFamily="sans-serif-medium" '
            + ('' if 'letterSpacing' in extra else f'{A}letterSpacing="0.08" ') +
            f'{A}shadowColor="#73000000" {A}shadowRadius="4" {A}shadowDy="1" {extra}/>')

def sep(st, size):
    if st['sep'] == 'line':
        return (f'<TextView {A}layout_width="1dp" {A}layout_height="{int(size*1.7)}dp" {A}layout_marginHorizontal="10dp" '
                f'{A}layout_marginBottom="10dp" {A}background="#5CFFFFFF" />')
    return num(dict(st, color='#80FFFFFF' if not st.get('glow') else '#80FFD9A8', clock='#80FFFFFF'), '', int(size * 0.85),
               f'{A}text=":" {A}layout_marginHorizontal="3dp" {A}layout_marginBottom="12dp"')

def col(st, id_, lab, size):
    return (f'<LinearLayout {A}layout_width="wrap_content" {A}layout_height="wrap_content" {A}orientation="vertical" {A}gravity="center_horizontal">'
            f'{num(st, id_, size)}{label(lab, extra=f"{A}layout_marginTop=\"3dp\"")}</LinearLayout>')

def bar():
    return (f'<ProgressBar {A}id="@+id/progress" style="@android:style/Widget.Material.ProgressBar.Horizontal" '
            f'{A}layout_width="match_parent" {A}layout_height="4dp" {A}layout_marginTop="10dp" {A}max="1000" '
            f'{A}progressTint="#FFFFFFFF" {A}progressBackgroundTint="#55FFFFFF" {A}visibility="gone" />'
            + label('', 10, f'{A}id="@+id/caption" {A}layout_marginTop="5dp" {A}letterSpacing="0" {A}visibility="gone"'))

def frame(inner, pad='12dp'):
    return f'''<?xml version="1.0" encoding="utf-8"?>
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android" android:id="@+id/root"
    android:layout_width="match_parent" android:layout_height="match_parent">
    <ImageView android:id="@+id/bg" android:layout_width="match_parent" android:layout_height="match_parent"
        android:src="@drawable/widget_bg" android:scaleType="fitXY" android:importantForAccessibility="no" />
    <LinearLayout android:id="@+id/content" android:layout_width="match_parent" android:layout_height="wrap_content"
        android:layout_gravity="center" android:padding="{pad}" android:orientation="vertical" android:gravity="center_horizontal">
{inner}
    </LinearLayout>
    <TextView android:id="@+id/setup" android:layout_width="wrap_content" android:layout_height="wrap_content"
        android:layout_gravity="center" android:gravity="center" android:padding="12dp" android:visibility="gone"
        android:text="@string/setup_widget" android:textColor="#FFFFFFFF" android:textSize="13sp"
        android:shadowColor="#73000000" android:shadowRadius="6" />
</FrameLayout>
'''

def row(*items, extra=''):
    gravity = '' if 'gravity=' in extra else f'{A}gravity="center_vertical" '
    return (f'<LinearLayout {A}layout_width="wrap_content" {A}layout_height="wrap_content" {A}orientation="horizontal" '
            f'{gravity}{extra}>' + ''.join(items) + '</LinearLayout>')

def build(key, size):
    st = STYLES[key]
    if size == 'compact':
        s = 17
        inner = row(num(st, 'years', s), num(st, 'months', s, f'{A}layout_marginStart="8dp"'),
                    num(st, 'days', s, f'{A}layout_marginStart="8dp"'),
                    num(st, 'clock', s, f'{A}layout_marginStart="10dp"')) + bar()
        return frame(inner, '8dp')
    if size == 'small':
        big = 46 if key == 'hero' else 38
        inner = (row(num(st, 'years', big), label('years', 12, f'{A}layout_marginStart="4dp" {A}letterSpacing="0"'),
                     extra=f'{A}gravity="bottom"')
                 + row(num(st, 'months', 14), label('  ·  ', 14), num(st, 'days', 14), extra=f'{A}layout_marginTop="2dp"')
                 + num(st, 'clock', 20, f'{A}layout_marginTop="4dp"') + bar())
        return frame(inner, '10dp')
    if size == 'wide':
        if key == 'hero':
            right = (f'<LinearLayout {A}layout_width="wrap_content" {A}layout_height="wrap_content" {A}orientation="vertical" {A}layout_marginStart="14dp">'
                     + row(num(st, 'months', 18), label(' mo   ', 11), num(st, 'days', 18), label(' d', 11), extra=f'{A}gravity="bottom"')
                     + num(st, 'clock', 22, f'{A}layout_marginTop="4dp"') + '</LinearLayout>')
            inner = row(num(st, 'years', 60), label('years', 13, f'{A}layout_marginStart="4dp" {A}letterSpacing="0"'), right,
                        extra=f'{A}gravity="center_vertical"') + bar()
            return frame(inner)
        s = 24 if key != 'led' else 22
        inner = row(col(st, 'years', 'YEARS', s), sep(st, s), col(st, 'months', 'MONTHS', s), sep(st, s),
                    col(st, 'days', 'DAYS', s), sep(st, s),
                    f'<LinearLayout {A}layout_width="wrap_content" {A}layout_height="wrap_content" {A}orientation="vertical" {A}gravity="center_horizontal">'
                    + num(st, 'clock', s) + label('HRS : MIN : SEC', extra=f'{A}layout_marginTop="3dp"') + '</LinearLayout>') + bar()
        return frame(inner)
    # large
    big = 80 if key == 'hero' else 62
    inner = (row(num(st, 'years', big), label('years', 16, f'{A}layout_marginStart="6dp" {A}layout_marginBottom="8dp" {A}letterSpacing="0"'),
                 extra=f'{A}gravity="bottom"')
             + row(col(st, 'months', 'MONTHS', 26), sep(st, 26) if st['sep'] == 'line' else label('', 9, f'{A}layout_marginHorizontal="14dp"'),
                   col(st, 'days', 'DAYS', 26), extra=f'{A}layout_marginTop="8dp"')
             + num(st, 'clock', 30, f'{A}layout_marginTop="10dp"')
             + label('HOURS : MINUTES : SECONDS', extra=f'{A}layout_marginTop="3dp"') + bar())
    return frame(inner, '16dp')

for key in STYLES:
    for size in SIZES:
        with open(os.path.join(LAYOUT, f'w_{key}_{size}.xml'), 'w') as f:
            f.write(build(key, size))

lines = ['package com.manoj.agecounter', '', '// Generated by tools/gen_layouts.py. Do not edit by hand.', '',
         'enum class WidgetStyle(val key: String, val title: String) {']
lines += [f'    {k.upper()}("{k}", "{v["name"]}"),' for k, v in STYLES.items()]
lines[-1] = lines[-1].rstrip(',') + ';'
lines += ['', '    fun layout(size: String): Int = when (this) {']
for k in STYLES:
    lines.append(f'        {k.upper()} -> when (size) {{ ' + '; '.join(f'"{s}" -> R.layout.w_{k}_{s}' for s in SIZES[:-1])
                 + f'; else -> R.layout.w_{k}_large }}')
lines += ['    }', '', '    companion object {', '        fun of(key: String?) = values().firstOrNull { it.key == key } ?: ODOMETER', '    }', '}', '']
open(KT, 'w').write('\n'.join(lines))
print('generated', len(STYLES) * len(SIZES), 'layouts')
