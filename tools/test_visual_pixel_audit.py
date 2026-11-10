#!/usr/bin/env python3
"""Regression tests for exact native-resolution owner/screenshot comparisons."""
import sys
import unittest
from pathlib import Path

from PIL import Image

sys.path.insert(0, str(Path(__file__).resolve().parent))
from visual_pixel_audit import exact_pixel_difference, rgb_mae


class PixelDifferenceTests(unittest.TestCase):
    def test_identical_rgb_images_have_zero_different_pixels(self):
        original = Image.new("RGB", (4, 3), (255, 120, 80))
        different, total = exact_pixel_difference(original, original.copy())
        self.assertEqual((different, total), (0, 12))
        self.assertEqual(rgb_mae(original, original.copy()), 0.0)

    def test_one_modified_channel_counts_as_one_changed_pixel(self):
        original = Image.new("RGB", (4, 3), (0, 0, 0))
        actual = original.copy()
        actual.putpixel((2, 1), (0, 0, 1))
        self.assertEqual(exact_pixel_difference(original, actual), (1, 12))
        self.assertAlmostEqual(rgb_mae(original, actual), 1.0 / 36.0)

    def test_exact_roi_does_not_contain_outside_changes(self):
        original = Image.new("RGB", (4, 3), (0, 0, 0))
        actual = original.copy()
        actual.putpixel((3, 2), (255, 20, 30))
        self.assertEqual(exact_pixel_difference(original, actual, (0, 0, 3, 2)), (0, 6))
        self.assertEqual(exact_pixel_difference(original, actual, (3, 2, 4, 3)), (1, 1))

    def test_mismatched_size_cannot_be_silently_rescaled(self):
        with self.assertRaises(ValueError):
            exact_pixel_difference(Image.new("RGB", (3, 3)),
                                   Image.new("RGB", (4, 3)))
        with self.assertRaises(ValueError):
            rgb_mae(Image.new("RGB", (3, 3)), Image.new("RGB", (4, 3)))

    def test_invalid_roi_is_rejected(self):
        image = Image.new("RGB", (4, 3))
        for bounds in ((0, 0, 5, 3), (-1, 0, 2, 2), (2, 2, 2, 3)):
            with self.assertRaises(ValueError):
                exact_pixel_difference(image, image, bounds)


if __name__ == "__main__":
    unittest.main()
