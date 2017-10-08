package org.economicsl.settlement

import org.economicsl.core.Quantity
import org.economicsl.core.securities.Security


case class Payment(currency: Security, quantity: Quantity)

