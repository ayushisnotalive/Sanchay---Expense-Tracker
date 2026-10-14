const express = require("express");
const cors = require("cors");
const dotenv = require("dotenv");
const { PrismaClient } = require("@prisma/client");

dotenv.config();

const app = express();
const prisma = new PrismaClient();
const PORT = process.env.PORT || 3000;

app.use(cors());
app.use(express.json());

// Health check
app.get("/api/health", (req, res) => {
  res.json({ status: "UP", database: "Neon PostgreSQL", orm: "Prisma" });
});

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

async function handleRegisterOrLogin(req, res) {
  try {
    const { email, name } = req.body;
    if (!email || typeof email !== "string" || !EMAIL_REGEX.test(email.trim())) {
      return res.status(400).json({ error: "Valid email address required" });
    }

    const cleanEmail = email.trim().toLowerCase();
    const cleanName = (name && typeof name === "string") ? name.trim().slice(0, 100) : null;

    // Check if user exists or create new with unique email
    const user = await prisma.user.upsert({
      where: { email: cleanEmail },
      update: {
        name: cleanName,
        status: "ACTIVE",
        lastLoginAt: new Date(),
        isOneTimeLoginCompleted: true,
        scheduledDeletionAt: null,
        deactivatedUntil: null,
        deactivatedAt: null
      },
      create: {
        email: cleanEmail,
        name: cleanName,
        status: "ACTIVE",
        isOneTimeLoginCompleted: true
      }
    });

    res.json({
      success: true,
      user: {
        id: user.id,
        email: user.email,
        name: user.name,
        status: user.status,
        isOneTimeLoginCompleted: user.isOneTimeLoginCompleted,
        scheduledDeletionAt: user.scheduledDeletionAt,
        deactivatedUntil: user.deactivatedUntil
      }
    });
  } catch (error) {
    console.error("Registration error:", error);
    res.status(500).json({ error: "Internal server error" });
  }
}

// 1. User Registration / Sovereign Identity
app.post("/api/auth/register", handleRegisterOrLogin);
app.post("/api/auth/google-oauth", handleRegisterOrLogin); // backward compatibility alias

// 2. Account Deactivation (3 days, 7 days, or Until Turned On)
app.post("/api/account/deactivate", async (req, res) => {
  try {
    const { email, confirmation, duration } = req.body;

    if (confirmation !== "deactivate") {
      return res.status(400).json({ error: "Must confirm with 'deactivate'" });
    }

    if (!email) {
      return res.status(400).json({ error: "Email is required" });
    }

    const cleanEmail = email.trim().toLowerCase();
    const now = new Date();
    let status = "DEACTIVATED_UNTIL_MANUAL_REACTIVATION";
    let deactivatedUntil = null;

    if (duration === "3_DAYS") {
      status = "DEACTIVATED_3_DAYS";
      deactivatedUntil = new Date(now.getTime() + 3 * 24 * 60 * 60 * 1000);
    } else if (duration === "7_DAYS") {
      status = "DEACTIVATED_7_DAYS";
      deactivatedUntil = new Date(now.getTime() + 7 * 24 * 60 * 60 * 1000);
    }

    const user = await prisma.user.upsert({
      where: { email: cleanEmail },
      update: {
        status,
        deactivatedAt: now,
        deactivatedUntil
      },
      create: {
        email: cleanEmail,
        status,
        deactivatedAt: now,
        deactivatedUntil
      }
    });

    res.json({
      success: true,
      message: "Account deactivated successfully",
      user: {
        email: user.email,
        status: user.status,
        deactivatedUntil: user.deactivatedUntil
      }
    });
  } catch (error) {
    console.error("Deactivate error:", error);
    res.status(500).json({ error: "Internal server error" });
  }
});

// 3. Account Reactivation
app.post("/api/account/reactivate", async (req, res) => {
  try {
    const { email } = req.body;
    if (!email) return res.status(400).json({ error: "Email is required" });

    const cleanEmail = email.trim().toLowerCase();
    const user = await prisma.user.update({
      where: { email: cleanEmail },
      data: {
        status: "ACTIVE",
        deactivatedAt: null,
        deactivatedUntil: null,
        scheduledDeletionAt: null
      }
    });

    res.json({ success: true, message: "Account reactivated successfully", user });
  } catch (error) {
    console.error("Reactivate error:", error);
    res.status(500).json({ error: "Internal server error" });
  }
});

// 4. Account Deletion (Scheduled 15 days range from deletion request)
app.post("/api/account/delete", async (req, res) => {
  try {
    const { email, confirmation } = req.body;

    if (confirmation !== "delete") {
      return res.status(400).json({ error: "Must confirm with 'delete'" });
    }

    if (!email) {
      return res.status(400).json({ error: "Email is required" });
    }

    const cleanEmail = email.trim().toLowerCase();
    const now = new Date();
    // 15 days grace period range
    const scheduledDeletionAt = new Date(now.getTime() + 15 * 24 * 60 * 60 * 1000);

    const user = await prisma.user.upsert({
      where: { email: cleanEmail },
      update: {
        status: "PENDING_DELETION",
        scheduledDeletionAt
      },
      create: {
        email: cleanEmail,
        status: "PENDING_DELETION",
        scheduledDeletionAt
      }
    });

    res.json({
      success: true,
      message: "Account scheduled for permanent deletion in 15 days",
      scheduledDeletionAt: user.scheduledDeletionAt
    });
  } catch (error) {
    console.error("Delete error:", error);
    res.status(500).json({ error: "Internal server error" });
  }
});

// 5. Account Status
app.get("/api/account/status", async (req, res) => {
  try {
    const email = req.query.email;
    if (!email) return res.status(400).json({ error: "Email required" });

    const user = await prisma.user.findUnique({
      where: { email: email.trim().toLowerCase() }
    });

    if (!user) {
      return res.status(404).json({ error: "User not found" });
    }

    res.json({ success: true, user });
  } catch (error) {
    console.error("Status error:", error);
    res.status(500).json({ error: "Internal server error" });
  }
});

app.listen(PORT, () => {
  console.log(`Sanchay API Server running on port ${PORT} with Neon DB`);
});
