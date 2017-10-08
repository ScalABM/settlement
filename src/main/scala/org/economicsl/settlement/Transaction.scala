package org.economicsl.settlement

import java.util.UUID

import org.economicsl.core.Currency


/** Represents a cleared transaction between a buyer and a seller. */
case class Transaction(from: UUID, to: UUID, value: Currency)